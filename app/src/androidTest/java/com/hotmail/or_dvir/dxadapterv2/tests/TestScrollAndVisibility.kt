package com.hotmail.or_dvir.dxadapterv2.tests

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.hotmail.or_dvir.dxadapterv2.R
import com.hotmail.or_dvir.dxadapterv2.draggable.AdapterDraggable
import com.hotmail.or_dvir.dxadapterv2.draggable.ItemDraggable
import com.hotmail.or_dvir.dxrecyclerview.DxScrollListener
import com.hotmail.or_dvir.dxrecyclerview.DxVisibilityListener
import com.hotmail.or_dvir.dxrecyclerview.GenericListener
import io.mockk.spyk
import io.mockk.verify
import kotlinx.android.synthetic.main.activity_base.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class TestScrollAndVisibility : BaseTest() {
    //region visibility listeners
    private lateinit var mFirstVisible: GenericListener
    private lateinit var mFirstInvisible: GenericListener

    private lateinit var mLastVisible: GenericListener
    private lateinit var mLastInvisible: GenericListener
    //endregion

    //region scroll listeners
    private lateinit var mOnScrollUp: GenericListener
    private lateinit var mOnScrollDown: GenericListener
    private lateinit var mOnScrollLeft: GenericListener
    private lateinit var mOnScrollRight: GenericListener
    //endregion

    @Before
    fun before() {
        //set a fresh empty list.
        val testAdapter = AdapterDraggable(mutableListOf())

        onActivity {
            it.apply {
                //register idling resource
                IdlingRegistry.getInstance()
                    .register(activityBase_rv.idlingResource.resource)

                setAdapter(testAdapter)
            }
        }
    }

    @After
    fun after() {
        //unregister idling resource
        onActivity {
            IdlingRegistry.getInstance().unregister(it.activityBase_rv.idlingResource.resource)
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
        onView(withId(R.id.activityBase_rv)).perform(
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
        onView(withId(R.id.activityBase_rv)).perform(
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

    @Test
    fun scrollListenerTest_highSensitivity() {
        //high sensitivity
        setupScrollListeners(200)
        val listSize = 100
        setListForActivity(listSize)

        onActivity { it.setLayoutManagerVertical() }

        //NOTE:
        //this function only tests that a slow scroll does not trigger listener with
        //high sensitivity.
        //all other scroll tests are performed in their own functions


        //scroll down slow
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        onView(withId(R.id.activityBase_rv)).perform(
            swipeUpSlow()
        )

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollDown invoked (will be invoked many times)
        verify(exactly = 0) { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }
    }

    @Test
    fun scrollListenerTest_vertical() {
        //low sensitivity to guarantee listeners will be called
        setupScrollListeners(1)
        val listSize = 100
        setListForActivity(listSize)

        onActivity { it.setLayoutManagerVertical() }

        //verify no listeners have been invoked
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }

        //scroll down fast
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        onView(withId(R.id.activityBase_rv)).perform(swipeUpFast())

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollDown invoked (will be invoked many times)
        verify { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }

        //scroll up fast
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        onView(withId(R.id.activityBase_rv)).perform(swipeDownFast())

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollUp invoked (will be invoked many times).
        //NOTE: there is no way to reset the "call count" of mockk.verify{},
        //so we most account for previous invocations
        verify { mOnScrollUp.invoke() }
        verify { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }
    }

    @Test
    fun scrollListenerTest_horizontal() {
        //low sensitivity to guarantee listeners will be called
        setupScrollListeners(1)
        val listSize = 100
        setListForActivity(listSize)

        onActivity { it.setLayoutManagerHorizontal() }

        //verify no listeners have been invoked
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }
        verify(exactly = 0) { mOnScrollRight.invoke() }

        //scroll to end of list.
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        onView(withId(R.id.activityBase_rv)).perform(ViewActions.swipeLeft())

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollRight invoked (will be invoked many times)
        verify { mOnScrollRight.invoke() }
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollDown.invoke() }
        verify(exactly = 0) { mOnScrollLeft.invoke() }

        //scroll to top of list
        //NOTE: using swipe action and not scrollToPosition() because scrollToPosition()
        //does not trigger the scroll listener properly (dx and dy values are 0)
        onView(withId(R.id.activityBase_rv)).perform(ViewActions.swipeRight())

        //wait for the scroll to finish
        pauseTestUntilAsyncOperationDone()

        //verify only mOnScrollLeft invoked (will be invoked many times).
        //NOTE: there is no way to reset the "call count" of mockk.verify{},
        //so we most account for previous invocations
        verify { mOnScrollLeft.invoke() }
        verify { mOnScrollRight.invoke() }
        verify(exactly = 0) { mOnScrollUp.invoke() }
        verify(exactly = 0) { mOnScrollDown.invoke() }
    }

    private fun setupScrollListeners(sensitivity: Int) {
        mOnScrollUp = spyk({})
        mOnScrollDown = spyk({})
        mOnScrollLeft = spyk({})
        mOnScrollRight = spyk({})

        onActivity {
            it.activityBase_rv.onScrollListener = DxScrollListener(sensitivity).apply {
                onScrollUp = mOnScrollUp
                onScrollDown = mOnScrollDown
                onScrollLeft = mOnScrollLeft
                onScrollRight = mOnScrollRight
            }
        }
    }

    //region helper functions
    /**
     * perform a dummy test that should always pass in order to make espresso
     * wait until all idling resources have finished
     */
    private fun pauseTestUntilAsyncOperationDone() {
        //the recycler view should always be visible, so this is a simple test
        //that should always pass
        onView(withId(R.id.activityBase_rv)).check(
            ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
        )
    }

    private fun setListForActivity(listSize: Int) {
        onActivity {
            it.apply {
                setAdapterItems((MutableList(listSize) { index -> ItemDraggable("item $index") }))
            }
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
            it.activityBase_rv.onItemsVisibilityListener = DxVisibilityListener(
                onFirstItemVisible = mFirstVisible,
                onFirstItemInvisible = mFirstInvisible,
                onLastItemVisible = mLastVisible,
                onLastItemInvisible = mLastInvisible
            )
        }
    }

    private fun swipe(speed: Swipe, from: GeneralLocation, to: GeneralLocation) =
        GeneralSwipeAction(speed, from, to, Press.FINGER)

    private fun swipeUp(speed: Swipe) =
        swipe(speed, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER)

    private fun swipeDown(speed: Swipe) =
        swipe(speed, GeneralLocation.TOP_CENTER, GeneralLocation.BOTTOM_CENTER)

    private fun swipeUpFast() = swipeUp(Swipe.FAST)
    private fun swipeUpSlow() = swipeUp(Swipe.SLOW)

    private fun swipeDownFast() = swipeDown(Swipe.FAST)

    @Suppress("unused")
    private fun swipeDownSlow() = swipeDown(Swipe.SLOW)
    //endregion
}
