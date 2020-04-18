package com.hotmail.or_dvir.dxlibraries

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hotmail.or_dvir.dxrecyclerview.DxRecyclerView
import com.hotmail.or_dvir.dxrecyclerview.DxScrollListener
import com.hotmail.or_dvir.dxrecyclerview.DxVisibilityListener
import com.hotmail.or_dvir.dxrecyclerview.EmptyListener
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.CoreMatchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TestDxRecyclerView {
    //visibility listeners
    private lateinit var mFirstVisible: EmptyListener
    private lateinit var mFirstInvisible: EmptyListener

    private lateinit var mLastVisible: EmptyListener
    private lateinit var mLastInvisible: EmptyListener

    //scroll listeners
    private lateinit var mOnScrollUp: EmptyListener
    private lateinit var mOnScrollDown: EmptyListener
    private lateinit var mOnScrollLeft: EmptyListener
    private lateinit var mOnScrollRight: EmptyListener


    @get:Rule
    var activityScenario = ActivityScenarioRule(ActivityMain::class.java)

    private fun onActivity(task: (act: ActivityMain) -> Unit) =
        activityScenario.scenario.onActivity { task.invoke(it) }

    @Before
    fun before() {
        //register idling resource
        onActivity {
            IdlingRegistry.getInstance().register(it.activityMain_rv.getIdlingResourceInstance())
        }

        //set a fresh empty list.
        //IMPORTANT NOTE
        //this method should NOT be called from inside the activity (e.g. inside onActivity{} block)
        //or the test will get stuck!!!
        setListForActivity(0)
    }

    @After
    fun after() {
        //unregister idling resource
        onActivity {
            IdlingRegistry.getInstance().unregister(it.activityMain_rv.getIdlingResourceInstance())
        }
    }

    /**
     * perform a dummy test that should always pass in order to make espresso
     * wait until all idling resources have finished
     */
    private fun pauseTestUntilAsyncOperationDone() {
        //the recycler view should always be visible, so this is a simple test
        //that should always pass
        //todo why am i getting unresolved reference for this id?????
//        onView(withId(R.id.activityMain_rv)).check(
        onView(withClassName(containsString(DxRecyclerView::class.java.simpleName))).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )
    }

    private fun setListForActivity(listSize: Int) {
        onActivity {
            it.mAdapter.items = List(listSize) { index -> MyItem("item $index") }
        }

        //since the listeners may be called after a small delay, we need to wait for
        //idling resources in order for testing to not fail
        pauseTestUntilAsyncOperationDone()
    }

    private fun setupVisibilityListeners() {
        mFirstVisible = spyk({})
        mFirstInvisible = spyk({})

        mLastVisible = spyk({})
        mLastInvisible = spyk({})

        onActivity {
            it.activityMain_rv.onItemsVisibilityListener = DxVisibilityListener().apply {
                onFirstItemVisible = mFirstVisible
                onFirstItemInvisible = mFirstInvisible

                onLastItemVisible = mLastVisible
                onLastItemInvisible = mLastInvisible
            }
        }
    }

    @Test
    fun visibilityListenersTest_shortList() {
        setupVisibilityListeners()

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
    fun visibilityListenersTest_longList() {
        setupVisibilityListeners()

        //creating a long list that should not fit entirely on the screen
        val longListSize = 100
        setListForActivity(longListSize)

        //mFirstVisible and mLastInvisible should be invoked
        verify(exactly = 1) { mFirstVisible.invoke() }
        verify(exactly = 1) { mLastInvisible.invoke() }

        //mFirstInvisible and mLastVisible should NOT be invoked
        verify(exactly = 0) { mFirstInvisible.invoke() }
        verify(exactly = 0) { mLastVisible.invoke() }


        //scroll to end of list
        //todo why am i getting unresolved reference for this id?????
//        onView(withId(R.id.activityMain_rv)).perform(
        onView(withClassName(containsString(DxRecyclerView::class.java.simpleName))).perform(
            //NOTE: the position parameter must be within the recycler view bounds!
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(longListSize - 1)
        )

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //mFirstInvisible and mLastVisible should be called
        verify(exactly = 1) { mFirstInvisible.invoke() }
        verify(exactly = 1) { mLastVisible.invoke() }

        //mFirstVisible and mLastInvisible should NOT be called
        //NOTE: there is no way to reset the "call count" of mockk.verify{},
        //so we most account for previous invocations
        verify(atMost = 1) { mFirstVisible.invoke() }
        verify(atMost = 1) { mLastInvisible.invoke() }

        //scroll to top of list
        //todo why am i getting unresolved reference for this id?????
//        onView(withId(R.id.activityMain_rv)).perform(
        onView(withClassName(containsString(DxRecyclerView::class.java.simpleName))).perform(
            RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0)
        )

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //mFirstVisible and mLastInvisible should be called
        //NOTE: there is no way to reset the "call count" of mockk.verify{},
        //so we most account for previous invocations
        verify(exactly = 2) { mFirstVisible.invoke() }
        verify(exactly = 2) { mLastInvisible.invoke() }

        //mFirstInvisible and mLastVisible should NOT be called
        //NOTE: there is no way to reset the "call count" of mockk.verify{},
        //so we most account for previous invocations
        verify(exactly = 1) { mFirstInvisible.invoke() }
        verify(exactly = 1) { mLastVisible.invoke() }
    }

    private fun setupScrollListeners() {
        mOnScrollUp = spyk({})
        mOnScrollDown = spyk({})
        mOnScrollLeft = spyk({})
        mOnScrollRight = spyk({})

        //todo how to i test the sensitivity?!
        onActivity {
            it.activityMain_rv.onScrollListener = DxScrollListener(1).apply {
                onScrollUp = mOnScrollUp
                onScrollDown = mOnScrollDown
                onScrollLeft = mOnScrollLeft
                onScrollRight = mOnScrollRight
            }
        }
    }

    @Test
    fun scrollListenerTest_vertical() {
        setupScrollListeners()
        val listSize = 100
        setListForActivity(listSize)

        onActivity { it.setLayoutManagerVertical() }

        //verify no listeners have been invoked
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }

        //scroll to end of list.
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        //todo why am i getting unresolved reference for this id?????
//        onView(withId(R.id.activityMain_rv)).perform(
        onView(withClassName(containsString(DxRecyclerView::class.java.simpleName))).perform(
            ViewActions.swipeUp()
        )
        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollDown invoked (will be invoked many times)
        verify { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }

        //scroll to top of list
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        //todo why am i getting unresolved reference for this id?????
//        onView(withId(R.id.activityMain_rv)).perform(
        onView(withClassName(containsString(DxRecyclerView::class.java.simpleName))).perform(
            ViewActions.swipeDown()
        )

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollUp invoked (will be invoked many times).
        //NOTE: there is no way to reset the "call count" of mockk.verify{},
        //so we most account for previous invocations
        verify { mOnScrollUp.invoke() }
        verify { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }

        //todo can i test sensitivity?????
    }

    @Test
    fun scrollListenerTest_horizontal() {
        setupScrollListeners()
        setListForActivity(100)

        //todo
        // set layout manager to horizontal
        // no listeners have been called at first.
        // scroll right - only scroll right triggered
        // scroll left - only scroll left triggered

        //todo can i test sensitivity?????
    }
}
