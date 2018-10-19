package xlab.world.xlab.utils.rx

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Use SchedulerProvider configuration for Single
 */
fun <T> Single<T>.with(scheduler: SchedulerProvider): Single<T> = observeOn(scheduler.ui()).subscribeOn(scheduler.io())

fun <T> Observable<T>.with(scheduler: SchedulerProvider): Observable<T> = observeOn(scheduler.ui()).subscribeOn(scheduler.io())

fun Completable.with(scheduler: SchedulerProvider): Completable = observeOn(scheduler.ui()).subscribeOn(scheduler.io())