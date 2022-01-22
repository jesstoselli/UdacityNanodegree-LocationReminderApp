package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val reminderDTO = ReminderDTO(
        title = "Title",
        description = "Description",
        location = "Location",
        latitude = 45.toDouble(),
        longitude = 12.toDouble()
    )

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    @Before
    fun registerIdlingResources(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @Before
    fun setup() {
        stopKoin()

        val appModule = module {
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(appModule))
        }

        repository = GlobalContext.get().koin.get()

        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun remindersListFragment_noData() {
        // GIVEN - you have a ReminderListFragment but datasource has no data
        launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)

        // THEN - tv_noDataTextView will be displayed instead
        onView(withId(R.id.tv_noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun remindersListFragment_dataIsDisplayedInUI() {
        // GIVEN - you have a ReminderListFragment with a reminder
        val reminder1 = ReminderDTO(
            title = "Cardiff Castle",
            description = "Description",
            location = "Location",
            latitude = 22.toDouble(),
            longitude = 57.toDouble()
        )
        runBlocking {
            repository.saveReminder(reminder1)
        }

        // WHEN - ReminderListFragment is rendered on to the screen
        launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)

        // THEN - all info should be displayed inside list items
        onView(withId(R.id.tv_noDataTextView)).check(matches(not(isDisplayed())))
        onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_description)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_location)).check(matches(isDisplayed()))
    }

    @Test
    fun addReminderFAB_navigateToSaveReminderFragment() {
        val reminder1 = ReminderDTO(
            title = "Cardiff Castle",
            description = "Description",
            location = "Location",
            latitude = 22.toDouble(),
            longitude = 57.toDouble()
        )
        runBlocking {
            repository.saveReminder(reminder1)
        }

        // GIVEN - there's a ReminderListFragment with a reminder
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - user clicks on FAB to add a new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - app should navigate to SaveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}