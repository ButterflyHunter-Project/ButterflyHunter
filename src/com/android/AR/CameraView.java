package com.android.AR;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.ImageFormat;

public class CameraView extends SurfaceView//the regular camera view 
{
	Camera camera;
	SurfaceHolder previewHolder;
	public int screenWidth;
	public int screenHeight;

	//Callback for the surfaceholder
	SurfaceHolder.Callback surfaceHolderListener = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			camera=Camera.open();
			try
			{
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {

			}
		}


		public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h)
		{
			//Parameters params = camera.getParameters();
			//params.setPreviewSize(screenWidth, screenHeight);
			//params.setPictureFormat(ImageFormat.JPEG);
			// camera.setParameters(params);
			camera.startPreview();
		}
		
		public void surfaceDestroyed(SurfaceHolder arg0)
		{
			camera.stopPreview();
			camera.release();
		}
	};
	@SuppressWarnings("deprecation")
	public CameraView(Context ctx)
	{
		super(ctx);
		previewHolder = this.getHolder();
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		previewHolder.addCallback(surfaceHolderListener);
		setBackgroundColor(Color.TRANSPARENT);
	}
	public CameraView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	protected void onDraw (Canvas canvas)
	{
		super.onDraw(canvas);
	}
	public void closeCamera()
	{
		if(camera != null)
			camera.release();
	}
	public void dispatchDraw(Canvas c)
	{
		super.dispatchDraw(c);
	}
	public void close() {

	}
}
