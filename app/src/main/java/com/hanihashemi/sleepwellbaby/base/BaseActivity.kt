package com.hanihashemi.sleepwellbaby.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import dagger.android.AndroidInjection

/**
 * Created by hani on 12/24/17.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected abstract val layoutResource: Int

    open fun gatherArguments(bundle: Bundle) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(layoutResource)
        if (intent.extras != null)
            gatherArguments(intent.extras)
        customizeUI()
    }

    protected abstract fun customizeUI()
}