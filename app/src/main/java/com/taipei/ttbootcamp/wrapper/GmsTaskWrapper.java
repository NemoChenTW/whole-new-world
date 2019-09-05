package com.taipei.ttbootcamp.wrapper;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;

public class GmsTaskWrapper {
    public static <TResult> Single<TResult> asSingle(final Task<TResult> task) {
        return Single.create(new SingleOnSubscribe<TResult>() {
            @Override
            public void subscribe(SingleEmitter<TResult> emitter) throws Exception {
                task.addOnSuccessListener(new OnSuccessListener<TResult>() {
                    @Override
                    public void onSuccess(TResult tResult) {
                        emitter.onSuccess(tResult);
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        emitter.onError(e);
                    }
                });
            }
        });
    }

    public static <TResult> Observable<TResult> asObservable(final Task<TResult> task) {
        return Observable.create(new ObservableOnSubscribe<TResult>() {
            @Override
            public void subscribe(ObservableEmitter<TResult> emitter) throws Exception {
                task.addOnSuccessListener(new OnSuccessListener<TResult>() {
                    @Override
                    public void onSuccess(TResult tResult) {
                        emitter.onNext(tResult);
                        emitter.onComplete();
                    }
                });
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        emitter.onError(e);
                    }
                });
            }
        });
    }
}

