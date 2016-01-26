package de.pasligh.android.teamme.tools;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import de.pasligh.android.teamme.R;

public final class TTS_Tool implements TextToSpeech.OnInitListener {
	public ReentrantLock waitForInitLock = new ReentrantLock();
	private static TextToSpeech tts;
	private static TTS_Tool INSTANCE;
	private Activity owner;
	private Locale currentLocale = Locale.getDefault();
	private final String LANGUAGE_CHECK_TEXT = "This player is already assigned.";

	/**
	 * @return the currentLocale
	 */
	public Locale getCurrentLocale() {
		return currentLocale;
	}

	/**
	 * @param currentLocale
	 *            the currentLocale to set
	 */
	public void setCurrentLocale(Locale currentLocale) {
		if (tts != null) {
			this.currentLocale = currentLocale;
			
			// changes TTS to US voice, if there are no localized strings available
			if (currentLocale != Locale.US && owner != null) {
				if (owner.getString(R.string.playerAlreadyAssigned)
						.equals(LANGUAGE_CHECK_TEXT))
					currentLocale = Locale.US;
			}
			setTextToSpeechSettings(currentLocale);
		}
	}

	private int utteranceID = -1;

	private TTS_Tool(Activity p_owner) {
		super();
		owner = p_owner;
		initTTS(owner);
	}

	private void initTTS(Activity owner) {
		tts = new TextToSpeech(owner, this);
		waitForInitLock.lock();
	}

	public static TTS_Tool getInstance(Activity p_owner) {
		if (INSTANCE == null) {
			INSTANCE = new TTS_Tool(p_owner);
		}
		return INSTANCE;
	}

	@Override
	public void onInit(int status) {
		setCurrentLocale(currentLocale);
		if (status == TextToSpeech.SUCCESS) {
			waitForInitLock.unlock();
		}
	}

	private void setTextToSpeechSettings(final Locale locale) {
		Locale defaultOrPassedIn = locale;
		if (locale == null) {
			defaultOrPassedIn = Locale.getDefault();
		}
		// check if language is available
		switch (tts.isLanguageAvailable(defaultOrPassedIn)) {
		case TextToSpeech.LANG_AVAILABLE:
		case TextToSpeech.LANG_COUNTRY_AVAILABLE:
		case TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE:
			tts.setLanguage(locale);
			break;
		case TextToSpeech.LANG_MISSING_DATA:
			installTTS_Data();
			break;
		case TextToSpeech.LANG_NOT_SUPPORTED:
			if (locale.getLanguage() != Locale.US.getLanguage()) {
				setCurrentLocale(Locale.US);
			}
			break;
		}
	}

	private void installTTS_Data() {

		shutdown();
		AlertDialog alertDialog = new AlertDialog.Builder(owner).create();
		alertDialog.setTitle(owner.getString(R.string.app_name));
		alertDialog.setMessage(owner.getString(R.string.ttsmissing));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent installIntent = new Intent();
						installIntent
								.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
						owner.startActivity(installIntent);
					}
				});
		alertDialog.show();

	}

	public void shutdown() {
		try {
			tts.shutdown();
		} catch (Exception e) {
		}
		INSTANCE = null;
	}

	public boolean isSpeaking() {
		return tts.isSpeaking();
	}

	public int sprechen(String text, int QUEUE) {
		utteranceID++;
		if (waitForInitLock.isLocked()) {
			try {
				waitForInitLock.tryLock(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Log.e("INS", "interruped");
			}
			// unlock it here so that it is never locked again
			waitForInitLock.unlock();
		}

		tts.speak(text, QUEUE, null);
		return utteranceID;
	}
}
