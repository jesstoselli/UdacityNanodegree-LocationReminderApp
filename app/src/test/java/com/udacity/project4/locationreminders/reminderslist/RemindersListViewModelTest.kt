package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun baseSetup() {
        stopKoin()

        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun loadReminders_successful() = runBlockingTest {
        // GIVEN - a list of reminders that comes from fakeDataSource
        val reminder1 = fakeDataSource.reminderDTO.copy(title = "Millennium Centre")
        val reminder2 = fakeDataSource.reminderDTO.copy(title = "Millennium Stadium")
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)

        // WHEN - loadReminders is called
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).isNotNull()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).isNotEmpty()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue()).hasSize(2)
    }

    @Test
    fun loadReminders_emptyData() = runBlockingTest {
        // GIVEN - an empty reminders list
        fakeDataSource.deleteAllReminders()

        // WHEN - loadReminders is called
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isTrue()
    }


    @Test
    fun loadReminders_unsuccessful() = runBlockingTest {
        // GIVEN - fakeDataSource should not return a list of reminders
        fakeDataSource.shouldReturnError(true)

        // WHEN - loadReminders is called
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error")
    }

}