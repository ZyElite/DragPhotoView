##  仿微信图片预览手势  

> 本项目代码未做任何封装，仅用来学习与修改非常合适。适合初学者与项目中有需要的借鉴及修改。
> 
> 作者：ZyElite
> 
>     如要转载请注明出去


### 设计与编码
在编写项目之前，简单体验了一下微信的效果，发现微信的效果主要有以下俩点

> 1. 下滑图片缩放与背景透明度的修改
> 2. 上滑图片图片只做位移，松开手回归原位

确定主要做的功能之后，我们进入编码阶段。（只是简单的小功能，这里就不做设计阶段了）

#### 编码

* 在MainActivity的布局文件中放一个RecyclerView用来摆放图片并设置点击事件打开预览界面（PreviewActivity）。并把所点击图片的位置传递到预览界面（PreviewActivity）
* PreviewActivity布局只放一个ViewPager用来所图片预览时左右滚动效果，在适配器的主要就是用到接下来我们自定义DragPhotoView


这里我们继承开源库PhotoView来实现我们的功能。

在自定义View过程中一般只需关注onDraw(),dispatchTouchEvent(),onTouchEvent()这三个方法，所以我们在继承PhotoView只需关心在view绘制与事件分发的时候做处理即可。以下附上核心代码：

---
    override fun onDraw(canvas: Canvas) {
		//设置透明度
        mPaint?.alpha = mAlpha
		//画一个矩形
        canvas.drawRect(0F, 0F, mWidth.toFloat(), mHeight.toFloat(), mPaint)
		//平移画布
        canvas.translate(mTranslateX, mTranslateY)
		//缩放画布
        canvas.scale(mScale, mScale, (mWidth / 2).toFloat(), (mHeight / 2).toFloat())
        super.onDraw(canvas)
    }

---
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
				//获取手指按下的X,Y坐标
                mDownX = event.rawX
                mDownY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
				//获取在移动过程中的X,Y坐标
                val rawX = event.rawX
                val rawY = event.rawY
				//得出要平移的X，Y
                mTranslateY = rawY - mDownY
                mTranslateX = rawX - mDownX
                if (Math.abs(mTranslateY) > 10) {//当垂直方法移动距离大于10就做平移效果
					//计算平移的比例 这里可以自己设置
                    val percent = mTranslateY / MAX_TRANSLATEY
					//得出透明的alpha的值
                    mAlpha = (255 * (1 - percent)).toInt()
					//缩放比例
                    mScale = 1 - percent
                    if (mScale <= MIN_SCALE) { //最小缩放
                        mScale = MIN_SCALE
                    } else if (mScale > 1) {//最大就是不变
                        mScale = 1F
                    }
                    if (mAlpha > 255) {//不透明
                        mAlpha = 255
                    } else if (mAlpha < 0) {//完全透明
                        mAlpha = 0
                    }
                    invalidate()//重新绘制
                }

            }

            MotionEvent.ACTION_UP -> {
                if (mTranslateY > 0) {//当手指抬起时 垂直方向移动距离大于10执行退出方法
					//如果退出事件不为空执行退出事件
                    if (mOnExitClickListener != null) {
						//以下代码处理当图片滑大盘能够屏幕外的时候图片显示不全问题
						//设定最大的平移 x y 位置
                        val maxTranslateY = height / 2 * 1F - (height * mScale / 2)
                        val maxTranslateX = width / 2 * 1F - (width * mScale / 2)
                        if (mTranslateY > maxTranslateY) {
                            mTranslateY = maxTranslateY
                            invalidate()
                        }
                        if (Math.abs(mTranslateX) > maxTranslateX) {
                            mTranslateX = if (mTranslateX < 0)
                                -maxTranslateX
                            else maxTranslateX
                            invalidate()
                        }
                        mAlpha = 0
                        invalidate()
                        mOnExitClickListener!!.onExit(this, mTranslateX, mTranslateY, mWidth, mHeight)
                        return true
                    }
                } else {//如果垂直方向位移小于0则回到原先的位置
                    mTranslateX = 0F
                    mTranslateY = 0F
                    invalidate()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


在图片关闭时，这里要回到当初所点击的位置。这个操作放到了adapter里实现，附上核心代码：

       override fun instantiateItem(container: ViewGroup, position: Int): Any {
			-----
            dragPhotoView.setOnExitListener(object : DragPhotoView.OnExitClickListener {
                override fun onExit(view: DragPhotoView, translateX: Float, translateY: Float, width: Int, height: Int) {
                    if (position == currentPosition) finish(width, translateX, height, translateY, view) else view.finishAnimator(mActivity!!)
                }

            })
			-----
            return dragPhotoView
        }

        private fun finish(width: Int, translateX: Float, height: Int, translateY: Float, view: DragPhotoView) {
			//这里需要计算点击图片的中心
            val targetX = location!![0] + mCurrentWidth / 2
            val targetY = location!![1] + mCurrentHeight / 2
            val mTranslateX = if (targetX > width / 2) {
                -translateX + (targetX - width / 2)
            } else {
                -translateX - Math.abs(targetX - width / 2)
            }
            val mTranslateY = if (targetY > height / 2) {
                -translateY + (targetY - height / 2)
            } else {
                -translateY - Math.abs(targetY - height / 2)
            }
			//并设置属性动画
            val animatorX = ValueAnimator.ofFloat(0F, mTranslateX)
            val animatorY = ValueAnimator.ofFloat(0F, mTranslateY)
            animatorX.duration = 500
            animatorY.duration = 500

            animatorX.addUpdateListener {
                view.x = it.animatedValue as Float
            }
            animatorY.addUpdateListener {
                view.y = it.animatedValue as Float
            }
            animatorX.start()
            animatorY.start()

            animatorY.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    mActivity!!.finish()
                    mActivity!!.overridePendingTransition(0, 0)
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
        }
 
暂时这样我们的效果就实现了，有疑问可以联系我。

附上实现效果：
     
<video height=498 width=510 controls="controls" src="‪https://github.com/ZyElite/DragPhotoView/blob/master/resources/preview.mp4">         
