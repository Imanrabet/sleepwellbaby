package com.hanihashemi.babysleep.base

import android.content.Intent
import android.support.v4.app.Fragment
import com.hanihashemi.babysleep.R

/**
 * Created by hani on 12/24/17.
 */
abstract class BaseActivityWithSingleFragment : BaseActivity() {
    private var fragmentTag: String? = null

    override val layoutResource
        get() = R.layout.activity_with_single_fragment

    protected val fragment: Fragment
        get() = supportFragmentManager.findFragmentByTag(fragmentTag)

    protected abstract fun createFragment(): Fragment

    override fun customizeUI() {
        val fragment = createFragment()
        fragmentTag = fragment.javaClass.simpleName

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment, fragmentTag)
                .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }
}