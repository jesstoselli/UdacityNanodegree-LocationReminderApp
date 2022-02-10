package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resumeDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun baseSetup() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun saveReminderViewModel_loadingSuccessful() {
        // GIVEN
        mainCoroutineRule.dispatcher.pauseDispatcher()

        // WHEN
        saveReminderViewModel.validateAndSaveReminder(fakeDataSource.reminderData)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()

        // THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun saveReminder_successful() {
        // GIVEN - a ReminderDataItem

        // WHEN
        saveReminderViewModel.saveReminder(fakeDataSource.reminderData)

        // THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved!"))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue()).isNotNull()
    }

    @Test
    fun validateAndSaveReminder_successful() {
        // GIVEN - a ReminderDataItem with all parameters
        val reminder = fakeDataSource.reminderData

        // WHEN - the user tries to save this reminder
        val returnValue = saveReminderViewModel.validateAndSaveReminder(reminder)

        // THEN - returns true
        assertThat(returnValue).isTrue()
    }

    @Test
    fun validateAndSaveReminder_emptyTitle() {
        // GIVEN - a ReminderDataItem with an empty title
        val reminder = fakeDataSource.reminderData.copy(title = "")

        // WHEN - the user tries to save this reminder
        val returnValue = saveReminderViewModel.validateAndSaveReminder(reminder)

        // THEN - throws an Snackbar error from fun validateEnteredData
        assertThat(returnValue).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }

    @Test
    fun validateAndSaveReminder_emptyLocation() {
        // GIVEN - a ReminderDataItem with an empty title
        val reminder = fakeDataSource.reminderData.copy(location = "")

        // WHEN - the user tries to save this reminder
        val returnValue = saveReminderViewModel.validateAndSaveReminder(reminder)

        // THEN - throws an Snackbar error from fun validateEnteredData
        assertThat(returnValue).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

}
