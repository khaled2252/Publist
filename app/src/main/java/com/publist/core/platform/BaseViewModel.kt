package com.publist.core.platform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.algolia.search.saas.AlgoliaException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.functions.FirebaseFunctionsException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    //    val error = MutableLiveData<Error>()
    val loading = MutableLiveData<Boolean>()
    val noInternetConnection = MutableLiveData<Boolean>()

    private fun handleError(exception: Throwable) {
//        if (exception is RetrofitException) {
//            when (exception.getKind()) {
//                RetrofitException.Kind.NETWORK ->
//                    error.postValue(Error(errorRes = R.string.error_no_connection))
//                RetrofitException.Kind.HTTP -> {
//                    exception.getResponse()?.let {
//                        if (it.code() == 401) {
//                            error.postValue(Error("unauthorized"))
//                        } else if (it.code() == 400 || it.code() == 409) {
//                            val jsonObject = JSONObject(it.errorBody()?.string())
//                            val responseData = jsonObject.getJSONObject("data")
//                            val errorType = responseData.getString("message")
//                            error.postValue(Error(errorType))
//                        } else if (it.code() == 404 || it.code() == 422) {
//                            val jsonObject = JSONObject(it.errorBody()?.string())
//                            val errorType = jsonObject.getString("message")
//                            error.postValue(Error(errorType))
//                        } else {
//                            error.postValue(Error(exception.message))
//                        }
//                    } ?: run {
//                        error.postValue(Error(exception.message))
//                    }
//                }
//                RetrofitException.Kind.UNEXPECTED ->
//                    error.postValue(Error(exception.message))
//            }
//        } else if (exception is ApplicationException) {
//            error.postValue(exception.error)
//        } else {
//            // todo
//        }
    }

    fun <T> subscribeObservable(
        observable: Observable<T>,
        success: Consumer<T>,
        error: Consumer<Throwable> = Consumer { },
        subscribeScheduler: Scheduler = Schedulers.io(),
        observeOnMainThread: Boolean = true,
        showLoading: Boolean = true
    ) {

        val observerScheduler =
            if (observeOnMainThread) AndroidSchedulers.mainThread()
            else subscribeScheduler

        compositeDisposable.add(
            observable
                .subscribeOn(subscribeScheduler)
                .observeOn(observerScheduler)
                .compose { single ->
                    composeObservable<T>(single, showLoading)
                }
                .subscribe(success, error)
        )
    }

    private fun <T> composeObservable(
        observable: Observable<T>,
        showLoading: Boolean
    ): Observable<T> {
        return observable
            .flatMap { item ->
                Observable.just(item)
                //todo response based on retrieved request
            }
            .doOnError {
                Timber.e(it)
                handleError(it)
            }
            .doOnSubscribe {
                loading.postValue(showLoading)
            }
            .doAfterTerminate {
                loading.postValue(false)
            }
    }

    fun subscribe(
        completable: Completable,
        success: Action,
        error: Consumer<Throwable> = Consumer { },
        subscribeScheduler: Scheduler = Schedulers.io(),
        observeOnMainThread: Boolean = true,
        showLoading: Boolean = true
    ) {
        val observerScheduler =
            if (observeOnMainThread) AndroidSchedulers.mainThread()
            else subscribeScheduler
        compositeDisposable.add(
            completable
                .subscribeOn(subscribeScheduler)
                .observeOn(observerScheduler)
                .compose { composeComplete(completable, showLoading) }
                .subscribe(success, error)
        )
    }

    fun <T> subscribe(
        single: Single<T>,
        success: Consumer<T>,
        error: Consumer<Throwable> = Consumer { },
        subscribeScheduler: Scheduler = Schedulers.io(),
        observeOnMainThread: Boolean = true,
        showLoading: Boolean = true
    ) {

        val observerScheduler =
            if (observeOnMainThread) AndroidSchedulers.mainThread()
            else subscribeScheduler

        compositeDisposable.add(
            single
                .subscribeOn(subscribeScheduler)
                .observeOn(observerScheduler)
                .compose { single ->
                    composeSingle<T>(single, showLoading)
                }
                .subscribe(success, error))
    }

    private fun <T> composeSingle(single: Single<T>, showLoading: Boolean = true): Single<T> {
        return single
            .flatMap { item ->
                Single.just(item)
                //todo response based on retrieved request
            }
            .doOnError {
                Timber.e(it)
                handleError(it)
                if (it is FirebaseNetworkException || //While logging in using google
                    it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                    it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.INTERNAL ||
                    it is AlgoliaException
                )
                    noInternetConnection.postValue(true)
            }
            .doOnSubscribe {
                loading.postValue(showLoading)
            }
            .doAfterTerminate {
                loading.postValue(false)
            }
    }

    private fun composeComplete(
        completable: Completable,
        showLoading: Boolean = true
    ): Completable {
        return completable.doOnError {
            Timber.e(it)
            handleError(it)
            if (it is FirebaseNetworkException ||
                it is FirebaseFirestoreException && it.code == FirebaseFirestoreException.Code.UNAVAILABLE ||
                it is FirebaseFunctionsException && it.code == FirebaseFunctionsException.Code.INTERNAL ||
                it is AlgoliaException
            )
                noInternetConnection.postValue(true)
        }.doOnSubscribe {
            loading.postValue(showLoading)
        }.doAfterTerminate {
            loading.postValue(false)
        }
    }

    fun addSubscription(disposable: Disposable?) {
        disposable?.let {
            compositeDisposable.add(it)
        }
    }

    fun clearSubscription() {
        if (compositeDisposable.isDisposed.not()) compositeDisposable.clear()
    }

    override fun onCleared() {
        clearSubscription()
        super.onCleared()
    }
}