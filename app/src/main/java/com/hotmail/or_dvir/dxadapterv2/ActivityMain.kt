package com.hotmail.or_dvir.dxadapterv2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.hotmail.or_dvir.dxadapterv2.clickable.ActivityClick
import com.hotmail.or_dvir.dxadapterv2.draggable.ActivityDrag
import com.hotmail.or_dvir.dxadapterv2.expandable.ActivityExpand
import com.hotmail.or_dvir.dxadapterv2.scrollandvisibility.ActivityScrollAndVisibility
import com.hotmail.or_dvir.dxadapterv2.selectable.ActivitySelect
import com.hotmail.or_dvir.dxadapterv2.stickyheader.ActivityStickyHeader
import com.hotmail.or_dvir.dxadapterv2.swipeable.ActivitySwipe
import kotlinx.android.synthetic.main.activity_main.*

open class ActivityMain : AppCompatActivity(), View.OnClickListener {

    //todo
    // should i add filtering feature? is that useful?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityMain_ll_allButtons.children.forEach {
            if (it is Button) {
                it.setOnClickListener(this@ActivityMain)
            }
        }
    }

    override fun onClick(v: View?) {
        if (v == null) {
            throw Exception("clicked view was null")
        }

        val clazz =
            when (v.id) {
                R.id.activityMain_btn_click -> ActivityClick::class.java
                R.id.activityMain_btn_drag -> ActivityDrag::class.java
                R.id.activityMain_btn_expand -> ActivityExpand::class.java
                R.id.activityMain_btn_scrollAndVisibility -> ActivityScrollAndVisibility::class.java
                R.id.activityMain_btn_select -> ActivitySelect::class.java
                R.id.activityMain_btn_stickyHeader -> ActivityStickyHeader::class.java
                R.id.activityMain_btn_swipe -> ActivitySwipe::class.java
                else -> throw Exception("did you forget to add the button to this \"when\" statement?")
            }

        val intent = Intent(v.context, clazz)
        startActivity(intent)
    }
}
