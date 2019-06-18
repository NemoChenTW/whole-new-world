package com.taipei.ttbootcamp.ttsengine;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSEngine {

    private TextToSpeech tts;

    public TTSEngine(Context context) {
        createLanguageTTS(context);
    }

    public void destroy() {
        if (tts != null) {
            tts.shutdown();
        }
    }

    public void say(String text, Locale locale) {
        if( tts.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_AVAILABLE ) {
            tts.setLanguage(locale);
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null );
    }

    private void createLanguageTTS(Context context) {
        if( tts == null ) {
            tts = new TextToSpeech(context, arg0 -> {
                if( arg0 == TextToSpeech.SUCCESS ) {
                    Locale l = Locale.US;
                    if( tts.isLanguageAvailable(l) == TextToSpeech.LANG_COUNTRY_AVAILABLE ) {
                        tts.setLanguage(l);
                    }
                }
            }
            );
        }
    }
}
