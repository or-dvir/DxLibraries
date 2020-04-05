package com.hotmail.or_dvir.dxlibraries

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hotmail.or_dvir.dxrecyclerview.DxVisibilityListener
import com.hotmail.or_dvir.dxrecyclerview.EmptyListener
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TestDxRecyclerView {
    private lateinit var mFirstVisible: EmptyListener
    private lateinit var mFirstInvisible: EmptyListener

    //todo delete this when done fixing bug
    private var counter = 0

    private lateinit var mLastVisible: EmptyListener
    private lateinit var mLastInvisible: EmptyListener

    @get:Rule
    var activityScenario = ActivityScenarioRule(ActivityMain::class.java)

    //call order to @Before method seems to be reversed so add
    //prefix to the method name accordingly

    @Before
    fun z_setupIndividualListeners() {
        mFirstVisible = spyk({})
        mFirstInvisible = spyk({})

        mLastVisible = spyk({
            counter++
            return@spyk
        })
        mLastInvisible = spyk({})

        counter = 0
    }

    //make sure this function runs second - add 'b' for name
    @Before
    fun y_setupVisibilityListener() {
        onActivity {
            it.activityMain_rv.onItemsVisibilityListener = DxVisibilityListener().apply {
                onFirstItemVisible = mFirstVisible
                onFirstItemInvisible = mFirstInvisible

                onLastItemVisible = mLastVisible
                onLastItemInvisible = mLastInvisible
            }
        }
    }

    private fun setListForActivity(listSize: Int) {
        onActivity {
            it.mAdapter.items = List(listSize) { index -> MyItem("items $index") }
        }
    }

    private fun onActivity(task: (act: ActivityMain) -> Unit) =
        activityScenario.scenario.onActivity { task.invoke(it) }

    @Test
    fun visibilityListeners_shortListTest() {
        //creating a short list so both first and last fit on the screen
        setListForActivity(2)

        //mFirstVisible and mLastVisible should be invoked
        verify(exactly = 1) { mFirstVisible.invoke() }
        verify(exactly = 1) { mLastVisible.invoke() }

        //mFirstInvisible and mLastInvisible should NOT be invoked
        verify(exactly = 0) { mFirstInvisible.invoke() }
        verify(exactly = 0) { mLastInvisible.invoke() }
    }

    @Test
    fun visibilityListeners_longListTest() {
        //creating a long list that should not fit entirely on the screen
        setListForActivity(100)

        //mFirstVisible and mLastInvisible should be invoked
        verify(exactly = 1) { mFirstVisible.invoke() }
        verify(exactly = 1) { mLastInvisible.invoke() }

        //mFirstInvisible and mLastVisible should NOT be invoked
        verify(exactly = 0) { mFirstInvisible.invoke() }


        verify(exactly = 0) { mLastVisible.invoke() }


        //todo for some reason i get unresolved reference R.id.activityMain_rv
//        onView(withId(R.id.activityMain_rv)).perform(
//        onView(withClassName(containsString(DxRecyclerView::class.java.simpleName))).perform(
//            RecyclerViewActions.scrollToPosition<ViewHolder>(50)
//        )

        //todo
        // set adapter with large list - make sure last item invisible not triggered
        // scroll all the way down - make sure first item invisible not triggered
        // set all inner listeners in visiblity listener
        // set adapter with small list - make sure first and last listeners are called
        //      and invisible not called
        // set adapter with large list - make sure last item visible not triggered
        // scroll all the way down - make sure first item invisible triggered and last item visible triggered
    }


    //todo DxVisibilityListener
    // no (inner) listeners are set - no listener is called (does that even make sense?)
    // all listeners are set - only relevant ones are called (check class documentation)
    // not all listeners are set - non-set listeners aren't called (does that even make sense?)

    //todo DxScrollListener
    // no (inner) listeners are set - no listener is called (does that even make sense?)
    // all listeners are set - only relevant ones are called (check class documentation)
    // not all listeners are set - non-set listeners aren't called (does that even make sense?)
    // how do i test the sensitivity???????
}
