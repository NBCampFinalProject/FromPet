package com.pet.frompet.ui.map

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

/**
멀티쓰레딩 환경에서 동ㅇ시성 보장하는 AtomicBoolean
false로 초기화 되어 있음
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)


    /**
     *  뷰가 활성화 상태 or setVlaue로 값이 바뀌었을 때 호출되는 observe 함수.
     *
     *  pending 변수가 true면 if문 내의 로직을 처리하고 false로 바꾼다는 것
     *
     *  setValue를 통해서만 pending 값이 true로 바뀌기 때문에,
     *  Configuration Changed 가 일어나도 pending 값은 false 이기에 observe가 데이터 전달 nono
     */

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }
        // Observe the internal MutableLiveData
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    /**
     *  LiveData로써 들고있는 데이터의 값을 변경하는 함수
     *  여기서 pending(AtomiBoolean)의 변수는 true로 바꾸어서
     *  observe 내에 if문 처리 하도록
     */
    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    /**
     * 데이터의 속성을 지정 안해줘도 call만으로 setValue 호출 가능
     */
    @MainThread
    fun call() {
        value = null
    }

    companion object {
        private const val TAG = "SingleLiveEvent"
    }
}