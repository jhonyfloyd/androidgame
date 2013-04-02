package com.example.game;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.scene.CameraScene;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.ui.activity.BaseSplashActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Display;
import android.view.KeyEvent;
import android.widget.Toast;

public class AndEngineSimpleGame extends BaseGameActivity implements
		IOnSceneTouchListener {

	private Camera mCamera;

	// Fondos a utilizar
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	private ChangeableText score;

	
	// texturas a utilizar en el proyecto
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TextureRegion mPlayerTextureRegion;
	private TextureRegion mProjectileTextureRegion;
	private TextureRegion mTargetTextureRegion;
	private TextureRegion mPausedTextureRegion;
	private TextureRegion mWinTextureRegion;
	private TextureRegion mFailTextureRegion;


	//la escena principal para el juego
	private Scene mMainScene;
	private Sprite player;

	// sprites de ganador y perdedor
	private Sprite winSprite;
	private Sprite failSprite;

	private LinkedList<Sprite> projectileLL;
	private LinkedList<Sprite> targetLL;
	private LinkedList<Sprite> projectilesToBeAdded;
	private LinkedList<Sprite> TargetsToBeAdded;
	private Sound shootingSound;
	private Music backgroundMusic;
	private boolean runningFlag = false;
	private boolean pauseFlag = false;
	private CameraScene mPauseScene;
	private CameraScene mResultScene;
	private int hitCount;
	private final int maxScore = 10;

	
	
	
	public Engine onLoadEngine() {

		
		//asignar los tamaños de la pantalla
		final Display display = getWindowManager().getDefaultDisplay();
		int cameraWidth = display.getWidth();
		int cameraHeight = display.getHeight();

		// Establecer el objeto camara del engine
		mCamera = new Camera(0, 0, cameraWidth, cameraHeight);

		// Engine with varius options
		return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT,
				new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera)
				.setNeedsSound(true).setNeedsMusic(true));
	}

	
	public void onLoadResources() {
		// se prepara un contenedor para la imagen,el objeto mBitmap...
		mBitmapTextureAtlas = new BitmapTextureAtlas(512, 512,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
	    //contenedor para el fondo
		mFontTexture = new BitmapTextureAtlas(256, 256,
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				
		// establecer un ruta de acceso rapido 
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		
		// cargar todas las imagenes 
		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "Player.png",
						0, 0);
		mProjectileTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"Projectile.png", 64, 0);
		mTargetTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "enemigo.png",
						128, 0);
		mPausedTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "paused.png",
						0, 64);
		mWinTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "win.png", 0,
						128);
		mFailTextureRegion = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "fail.png", 0,
						256);

		// Se prepara la imagen fuente
		mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT,
				Typeface.BOLD), 40, true, Color.BLACK);

		// cargar textures en el motor
		mEngine.getTextureManager().loadTexture(mBitmapTextureAtlas);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);

		SoundFactory.setAssetBasePath("mfx/");
		try {
			shootingSound = SoundFactory.createSoundFromAsset(mEngine
					.getSoundManager(), this, "pew_pew_lei.wav");
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MusicFactory.setAssetBasePath("mfx/");

		try {
			backgroundMusic = MusicFactory.createMusicFromAsset(mEngine
					.getMusicManager(), this, "background_music.wav");
			backgroundMusic.setLooping(true);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	public Scene onLoadScene() {
		
		mEngine.registerUpdateHandler(new FPSLogger());

		// crear una nueva escena para el menu "PAUSE"
		mPauseScene = new CameraScene(mCamera);
		/* para centrar la etiqueta en el medio*/
		final int x = (int) (mCamera.getWidth() / 2 - mPausedTextureRegion
				.getWidth() / 2);
		final int y = (int) (mCamera.getHeight() / 2 - mPausedTextureRegion
				.getHeight() / 2);
		
		
		
		 
		final Sprite pausedSprite = new Sprite(x, y, mPausedTextureRegion);
		mPauseScene.attachChild(pausedSprite);
		// makes the scene transparent
		mPauseScene.setBackgroundEnabled(false);

		// the results scene, for win/fail
		mResultScene = new CameraScene(mCamera);
		winSprite = new Sprite(x, y, mWinTextureRegion);
		failSprite = new Sprite(x, y, mFailTextureRegion);
		mResultScene.attachChild(winSprite);
		mResultScene.attachChild(failSprite);
		// hacer la escena transparante
		mResultScene.setBackgroundEnabled(false);

		winSprite.setVisible(false);
		failSprite.setVisible(false);

		// establece el color de fondo
		mMainScene = new Scene();
		mMainScene
				.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		mMainScene.setOnSceneTouchListener(this);

		// Coordenadas para la nave
		
		//final int PlayerX=(int) ((mCamera.getWidth()-this.mPlayerTextureRegion.getWidth())/2);
        //final int PlayerY=(int) ((mCamera.getHeight()-this.mPlayerTextureRegion.getHeight()*1.5));
		
		final int PlayerX = this.mPlayerTextureRegion.getWidth() / 2;
		final int PlayerY = (int) ((mCamera.getHeight() - mPlayerTextureRegion
				.getHeight()) / 2);

		// colocar la nave on la escena
		player = new Sprite(PlayerX, PlayerY, mPlayerTextureRegion);
		player.setScale(2);

		
		//inicializando variables
		projectileLL = new LinkedList<Sprite>();
		targetLL = new LinkedList<Sprite>();
		projectilesToBeAdded = new LinkedList<Sprite>();
		TargetsToBeAdded = new LinkedList<Sprite>();

		//Ajustes para anotar la puntuacion maxima ,se le da las coordenadas 
		//adecuadas para q aparesca en la pantalla
		score = new ChangeableText(0, 0, mFont, String.valueOf(maxScore));
		// repositioning the score later so we can use the score.getWidth()
		score.setPosition(mCamera.getWidth() - score.getWidth() - 5, 5);

		createSpriteSpawnTimeHandler();
		mMainScene.registerUpdateHandler(detect);

		// activar la musica de fondo
		backgroundMusic.play();
		// runningFlag = true;

		restart();
		return mMainScene;
	}


	public void onLoadComplete() {
	}

	// manejador de control del tiempo 
	IUpdateHandler detect = new IUpdateHandler() {
		
		public void reset() {
		}

		
		public void onUpdate(float pSecondsElapsed) {

			Iterator<Sprite> targets = targetLL.iterator();
			Sprite _target;
			boolean hit = false;

			// iteracion sobre la nave enemiga
			while (targets.hasNext()) {
				_target = targets.next();

				// si una nave enemiga sobrepasa el screen,se elimina y 
				//tambien puede llamar se al metodo fail
				
				if (_target.getX() <= -_target.getWidth()) {
					removeSprite(_target, targets);
					//fail();
					break;
				}
				Iterator<Sprite> projectiles = projectileLL.iterator();
				Sprite _projectile;
				// iterating over all the projectiles (bullets)
				while (projectiles.hasNext()) {
					_projectile = projectiles.next();

					// in case the projectile left the screen
					if (_projectile.getX() >= mCamera.getWidth()
							|| _projectile.getY() >= mCamera.getHeight()
									+ _projectile.getHeight()
							|| _projectile.getY() <= -_projectile.getHeight()) {
						removeSprite(_projectile, projectiles);
						continue;
					}

					// if the targets collides with a projectile, remove the
					// projectile and set the hit flag to true
					if (_target.collidesWith(_projectile)) {
						removeSprite(_projectile, projectiles);
						hit = true;
						break;
					}
				}

				/*si el proyectil logra impactar una nave enemiga,esta se elimina y se 
				  incrementa el contador de muertes*/
				if (hit) {
					removeSprite(_target, targets);
					hit = false;
					hitCount++;
					score.setText(String.valueOf(hitCount));
				}
			}

			// Si se llega al maximo score se envia la etiqueta win 
			if (hitCount >= maxScore) {
				win();
			}

			// el codigo de abajo permite evitar las excepciones
			projectileLL.addAll(projectilesToBeAdded);
			projectilesToBeAdded.clear();

			targetLL.addAll(TargetsToBeAdded);
			TargetsToBeAdded.clear();
		}
	};

	/* separar el sprite de la ecena y borrarlo del iterador */
	public void removeSprite(final Sprite _sprite, Iterator<Sprite> it) {
		runOnUpdateThread(new Runnable() {

			
			public void run() {
				mMainScene.detachChild(_sprite);
			}
		});
		it.remove();
	}


	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

		// Este metodo maneja el touch screen,permite detectar si se toca en la pantalla
		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			final float touchX = pSceneTouchEvent.getX();
			final float touchY = pSceneTouchEvent.getY();
			shootProjectile(touchX, touchY);
			return true;
		}
		return false;
	}

	
	/**Se dispara un proyectil desde la posicion del jugador hacia el area tocada*/
	private void shootProjectile(final float pX, final float pY) {

		int offX = (int) (pX - player.getX());
		int offY = (int) (pY - player.getY());
		if (offX <= 0)
			return;

		final Sprite projectile;
		// posicion del proyectil sobre la nave
		projectile = new Sprite(player.getX(), player.getY(),
				mProjectileTextureRegion.deepCopy());
		mMainScene.attachChild(projectile, 1);

		int realX = (int) (mCamera.getWidth() + projectile.getWidth() / 2.0f);
	//	int realX=0;
		float ratio = (float) offY / (float) offX;
		int realY = (int) ((realX * ratio) + projectile.getY());

		int offRealX = (int) (realX - projectile.getX());
		int offRealY = (int) (realY - projectile.getY());
		float length = (float) Math.sqrt((offRealX * offRealX)
				+ (offRealY * offRealY));
		float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
		float realMoveDuration = length / velocity;

		// se define un movimiento dirigido para la trayectoria del proyectil
		MoveModifier mod = new MoveModifier(realMoveDuration,
				projectile.getX(), realX, projectile.getY(), realY);
		projectile.registerEntityModifier(mod.deepCopy());

		projectilesToBeAdded.add(projectile);
		// plays a shooting sound
		shootingSound.play();
	}

	/*Agregamos una nave enemiga en una posicion aleatoria y lo hacemos mover
	    a lo largo del eje X*/
	
	
	
	public void addTarget() {
		Random rand = new Random();

		int x = (int) mCamera.getWidth() + mTargetTextureRegion.getWidth();
		int minY = mTargetTextureRegion.getHeight();
		int maxY = (int) (mCamera.getHeight() - mTargetTextureRegion
				.getHeight());
		
		int rangeY = maxY - minY;
		int y = rand.nextInt(rangeY) + minY;

		Sprite target = new Sprite(x, y, mTargetTextureRegion.deepCopy());
		mMainScene.attachChild(target);

		int minDuration = 2;
		int maxDuration = 4;
		int rangeDuration = maxDuration - minDuration;
		int actualDuration = rand.nextInt(rangeDuration) + minDuration;

		MoveXModifier mod = new MoveXModifier(actualDuration, target.getY(),
				-target.getWidth());
		target.registerEntityModifier(mod.deepCopy());

		TargetsToBeAdded.add(target);

	}

	
	/* manejador de tiempo para las naves enemigas ,un disparo cada segundo */
	private void createSpriteSpawnTimeHandler() {
		TimerHandler spriteTimerHandler;
		float mEffectSpawnDelay = 1f;

		spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true,
				new ITimerCallback() {

					
					public void onTimePassed(TimerHandler pTimerHandler) {

						addTarget();
					}
				});

		getEngine().registerUpdateHandler(spriteTimerHandler);
	}

	/*Reinicia el juego y limpia toda la pantalla*/
	
	public void restart() {

		runOnUpdateThread(new Runnable() {

			
			// para separar y reunir los sprites
			public void run() {
				mMainScene.detachChildren();
				mMainScene.attachChild(player, 0);
				mMainScene.attachChild(score);
			}
		});

		// resetear todo
		hitCount = 0;
		score.setText(String.valueOf(hitCount));
		projectileLL.clear();
		projectilesToBeAdded.clear();
		TargetsToBeAdded.clear();
		targetLL.clear();
	}

	/* Detiene el juego y la musica cuando finaliza la actividad*/
	
	protected void onPause() {
		if (runningFlag) {
			pauseMusic();
			if (mEngine.isRunning()) {
				pauseGame();
				pauseFlag = true;
			}
		}
		super.onPause();
	}

	

	public void onResumeGame() {
		super.onResumeGame();
		
		/* Se muestra un mensaje(toast) al regresar al juego*/
		if (runningFlag) {
			if (pauseFlag) {
				pauseFlag = false;
				Toast.makeText(this, "Menu button to resume",
						Toast.LENGTH_SHORT).show();
			} else {
				// in case the user clicks the home button while the game on the
				// resultScene
				resumeMusic();
				mEngine.stop();
			}
		} else {
			runningFlag = true;
		}
	}
	
	

	public void pauseMusic() {
		if (runningFlag)
			if (backgroundMusic.isPlaying())
				backgroundMusic.pause();
	}

	public void resumeMusic() {
		if (runningFlag)
			if (!backgroundMusic.isPlaying())
				backgroundMusic.resume();
	}

	public void fail() {
		if (mEngine.isRunning()) {
			winSprite.setVisible(false);
			failSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}

	public void win() {
		if (mEngine.isRunning()) {
			failSprite.setVisible(false);
			winSprite.setVisible(true);
			mMainScene.setChildScene(mResultScene, false, true, true);
			mEngine.stop();
		}
	}

	public void pauseGame() {
		if (runningFlag) {
			mMainScene.setChildScene(mPauseScene, false, true, true);
			mEngine.stop();
		}
	}
	
	public void unPauseGame(){
		mMainScene.clearChildScene();
	}


	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		// Si se presiona el boton "menu"
		if (pKeyCode == KeyEvent.KEYCODE_MENU
				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if (mEngine.isRunning() && backgroundMusic.isPlaying()) {
				pauseMusic();
				pauseFlag = true;
				pauseGame();
				Toast.makeText(this, "BOTON PARA REANUDAR",
						Toast.LENGTH_SHORT).show();
			} else {
				if (!backgroundMusic.isPlaying()) {
					unPauseGame();
					pauseFlag = false;
					resumeMusic();
					mEngine.start();
				}
				return true;
			}
			// Si el boton de regreso es presionado
		} else if (pKeyCode == KeyEvent.KEYCODE_BACK
				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {

			if (!mEngine.isRunning() && backgroundMusic.isPlaying()) {
				mMainScene.clearChildScene();
				mEngine.start();
				restart();
				return true;
			}
			return super.onKeyDown(pKeyCode, pEvent);
		}
		return super.onKeyDown(pKeyCode, pEvent);
	}
}

