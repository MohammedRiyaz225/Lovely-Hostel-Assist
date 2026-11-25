package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HostelFix", appName)
  }

  @Test
  fun `launch main activity`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }

  @Test
  fun `student login with registration number and self-chosen password succeeds`() = kotlinx.coroutines.runBlocking {
    val authRepo = com.example.data.repository.AuthRepository()
    val result = authRepo.loginWithRole("12201945", "my_custom_password_999", com.example.data.model.UserRole.STUDENT)
    org.junit.Assert.assertTrue(result.isSuccess)
    val user = result.getOrNull()
    org.junit.Assert.assertNotNull(user)
    assertEquals(com.example.data.model.UserRole.STUDENT, user?.role)
    assertEquals("12201945", user?.rollNumber)
  }

  @Test
  fun `warden login with employee id and self-chosen password succeeds`() = kotlinx.coroutines.runBlocking {
    val authRepo = com.example.data.repository.AuthRepository()
    val result = authRepo.loginWithRole("EMP-WD-999", "super_warden_pass", com.example.data.model.UserRole.WARDEN)
    org.junit.Assert.assertTrue(result.isSuccess)
    val user = result.getOrNull()
    org.junit.Assert.assertNotNull(user)
    assertEquals(com.example.data.model.UserRole.WARDEN, user?.role)
    assertEquals("EMP-WD-999", user?.rollNumber)
  }

  @Test
  fun `maintenance staff login with staff id and self-chosen password succeeds`() = kotlinx.coroutines.runBlocking {
    val authRepo = com.example.data.repository.AuthRepository()
    val result = authRepo.loginWithRole("EMP-MNT-555", "plumber_secret", com.example.data.model.UserRole.STAFF)
    org.junit.Assert.assertTrue(result.isSuccess)
    val user = result.getOrNull()
    org.junit.Assert.assertNotNull(user)
    assertEquals(com.example.data.model.UserRole.STAFF, user?.role)
    assertEquals("EMP-MNT-555", user?.rollNumber)
  }

  @Test
  fun `login with blank id fails with role-specific message`() = kotlinx.coroutines.runBlocking {
    val authRepo = com.example.data.repository.AuthRepository()
    val studentResult = authRepo.loginWithRole("", "password123", com.example.data.model.UserRole.STUDENT)
    org.junit.Assert.assertTrue(studentResult.isFailure)
    assertEquals("Please enter your Registration Number.", studentResult.exceptionOrNull()?.message)

    val wardenResult = authRepo.loginWithRole("", "password123", com.example.data.model.UserRole.WARDEN)
    org.junit.Assert.assertTrue(wardenResult.isFailure)
    assertEquals("Please enter your Employee ID.", wardenResult.exceptionOrNull()?.message)
  }

  @Test
  fun `viewModel login preserves custom registration and employee ids`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.HostelFixViewModel(app)

    var studentSuccess = false
    vm.login("12209988", "pass123", com.example.data.model.UserRole.STUDENT, onSuccess = { studentSuccess = true })
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(studentSuccess)
    assertEquals("12209988", vm.currentUser.value?.rollNumber)
    assertEquals(com.example.data.model.UserRole.STUDENT, vm.currentUser.value?.role)
    assertEquals("MY_ROOM", vm.filterState.value.scopeTab)

    var wardenSuccess = false
    vm.login("EMP-WD-999", "pass123", com.example.data.model.UserRole.WARDEN, onSuccess = { wardenSuccess = true })
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    org.junit.Assert.assertTrue(wardenSuccess)
    assertEquals("EMP-WD-999", vm.currentUser.value?.rollNumber)
    assertEquals(com.example.data.model.UserRole.WARDEN, vm.currentUser.value?.role)
    assertEquals("UNASSIGNED", vm.filterState.value.statusTab)
  }

  @Test
  fun `complaint repository generates unique non-colliding ids and persists resolvedAt`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.AppDatabase::class.java
    ).allowMainThreadQueries().build()

    try {
      val repo = com.example.data.repository.ComplaintRepository(db.complaintDao())
      com.example.data.local.AppDatabase.populateInitialData(db.complaintDao())

      val c1 = repo.createComplaint("Fan issue", "Fan wobbling", "BH-4", "204", "Test", "12209988")
      val c2 = repo.createComplaint("Tap issue", "Tap dripping", "BH-4", "204", "Test", "12209988")
      org.junit.Assert.assertNotEquals(c1.id, c2.id)
      org.junit.Assert.assertFalse(c1.id in listOf("CMP-1021", "CMP-1022", "CMP-1023", "CMP-1024", "CMP-1025", "CMP-1026", "CMP-1027"))
      org.junit.Assert.assertEquals(9, db.complaintDao().getComplaintCount())

      repo.updateStatus(c1.id, com.example.data.model.ComplaintStatus.RESOLVED, "Ramesh Kumar")
      val resolved = repo.getComplaintById(c1.id)
      org.junit.Assert.assertNotNull(resolved?.resolvedAt)
      assertEquals(com.example.data.model.ComplaintStatus.RESOLVED, resolved?.status)

      repo.updateStatus(c1.id, com.example.data.model.ComplaintStatus.PENDING, null)
      val reopened = repo.getComplaintById(c1.id)
      org.junit.Assert.assertNull(reopened?.resolvedAt)
    } finally {
      db.close()
    }
  }

  @Test
  fun `groq request json sanitizes welcome and duplicate user turns`() {
    val history = listOf(
      com.example.data.groq.ChatMessage(
        id = "welcome_1",
        role = com.example.data.groq.MessageRole.ASSISTANT,
        text = "Hello! Welcome"
      ),
      com.example.data.groq.ChatMessage(
        id = "u1",
        role = com.example.data.groq.MessageRole.USER,
        text = "First question"
      ),
      com.example.data.groq.ChatMessage(
        id = "a1",
        role = com.example.data.groq.MessageRole.ASSISTANT,
        text = "First answer"
      ),
      com.example.data.groq.ChatMessage(
        id = "u2",
        role = com.example.data.groq.MessageRole.USER,
        text = "Second question"
      )
    )
    val json = com.example.data.groq.GroqClient.buildRequestJson(
      history = history,
      userMessage = "Second question",
      modelId = com.example.data.groq.GroqModel.LLAMA_3_3_70B.modelId,
      mode = com.example.data.groq.AssistantMode.CAMPUS_GUIDE
    )
    assertEquals("llama-3.3-70b-versatile", json.getString("model"))
    val messages = json.getJSONArray("messages")
    assertEquals(4, messages.length())
    assertEquals("system", messages.getJSONObject(0).getString("role"))
    assertEquals("user", messages.getJSONObject(1).getString("role"))
    assertEquals("assistant", messages.getJSONObject(2).getString("role"))
    assertEquals("user", messages.getJSONObject(3).getString("role"))
    assertEquals("Second question", messages.getJSONObject(3).getString("content"))
  }

  @Test
  fun `api client rejects invalid backend urls safely`() {
    val original = com.example.data.api.ApiClient.getBaseUrl()
    org.junit.Assert.assertFalse(com.example.data.api.ApiClient.updateBaseUrl("   "))
    org.junit.Assert.assertFalse(com.example.data.api.ApiClient.updateBaseUrl("http://bad host name"))
    assertEquals(original, com.example.data.api.ApiClient.getBaseUrl())
  }
}
