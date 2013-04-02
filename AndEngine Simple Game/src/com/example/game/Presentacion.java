package com.example.game;

import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.ui.activity.BaseSplashActivity;

import android.app.Activity;

public class Presentacion extends BaseSplashActivity{

	
	private static final int SPLASH_DURATION = 3;
	private static final float SPLASH_SCALE_FROM = 0.5f;
	
	@Override
	protected Class<? extends Activity> getFollowUpActivity() {
		
		return AndEngineSimpleGame.class;
		
	}

	@Override
	protected ScreenOrientation getScreenOrientation() {
		return ScreenOrientation.LANDSCAPE;
		
	}

	@Override
	protected float getSplashDuration() {
		return SPLASH_DURATION;
		
	}

	@Override
	protected IBitmapTextureAtlasSource onGetSplashTextureAtlasSource() {
		
		return new AssetBitmapTextureAtlasSource(this, "gfx/phoenix_press.png");
	}
	
	protected float getSplashScaleFrom(){
		
		return SPLASH_SCALE_FROM;
	}
	
	
	

}
