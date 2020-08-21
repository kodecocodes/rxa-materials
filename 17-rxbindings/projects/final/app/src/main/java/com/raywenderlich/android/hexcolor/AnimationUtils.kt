package com.raywenderlich.android.hexcolor

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import io.reactivex.rxjava3.core.Observable

fun colorAnimator(fromColor: Int, toColor: Int): Observable<Int> {
    val valueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
    valueAnimator.duration = 250 // milliseconds
    val observable = Observable.create<Int> { emitter ->
        valueAnimator.addUpdateListener {
            emitter.onNext(it.animatedValue as Int)
        }
    }

    return observable.doOnSubscribe { valueAnimator.start() }
}
