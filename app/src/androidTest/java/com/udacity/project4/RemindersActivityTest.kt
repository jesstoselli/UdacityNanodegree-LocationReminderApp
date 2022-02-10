package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.ToastMatcher
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val reminderDTO = ReminderDTO(
        title = "Cardiff Castle",
        description = "Oldest castle in Wales.",
        location = "Location",
        latitude = 51.4822346,
        longitude = -3.1833654
    )

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun showRemindersScreenWithAReminder() {
        runBlocking {
            repository.saveReminder(reminderDTO)
        }

        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
        onView(withText("Cardiff Castle")).check(matches(isDisplayed()))
        onView(withId(R.id.tv_description)).check(matches(isDisplayed()))
        onView(withText("Oldest castle in Wales.")).check(matches(isDisplayed()))
        onView(withId(R.id.tv_location)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun navigateToSaveReminderScreenAndSaveANewReminder() {
        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.fab_saveReminder)).perform(click())

        // Testing Snackbar for empty title
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        onView(withId(R.id.et_reminderTitle)).perform(typeText("Millennium Centre"))
        onView(withId(R.id.et_reminderDescription))
            .perform(typeText("Wales Millennium Centre description."))

        Espresso.closeSoftKeyboard()
        Thread.sleep(1000)

        onView(withId(R.id.fab_saveReminder)).perform(click())

        // Testing Snackbar for empty location
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
        Thread.sleep(2000)

        onView(withId(R.id.tv_selectLocation)).perform(click())

        Thread.sleep(3000)
        onView(withId(R.id.fragment_map)).perform(longClick())

        onView(withId(R.id.btn_saveThisLocation)).perform(click())

        onView(withId(R.id.fab_saveReminder)).perform(click())

        // Testing Toast for reminder properly saved
        onView(withText(R.string.reminder_saved))
            .inRoot(ToastMatcher().apply {
                matches(isDisplayed())
            })
        Thread.sleep(5000)

        onView(withId(R.id.tv_noDataTextView))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        onView(withText("Millennium Centre")).check(matches(isDisplayed()))
        onView(withText("Wales Millennium Centre description.")).check(matches(isDisplayed()))

        activityScenario.close()
    }
}
