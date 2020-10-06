package com.gerardbradshaw.mixup.editor

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.gerardbradshaw.collageview.views.*
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkCollageHasAspectRatioSetTo
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkCollageIsBorderEnabled
import com.gerardbradshaw.mixup.R
import com.gerardbradshaw.mixup.ActivityTestUtil.checkOptionsMenuVisibility
import com.gerardbradshaw.mixup.ActivityTestUtil.countImagesOnDevice
import com.gerardbradshaw.mixup.ActivityTestUtil.pressOptionsMenuButton
import com.gerardbradshaw.mixup.BaseApplication
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.changeAllImages
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkAllImagesIsDefault
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkCollageHasActualAspectRatio
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.checkCollageTypeIs
import com.gerardbradshaw.mixup.editor.CollageViewTestUtil.setCollageAspectRatio
import com.gerardbradshaw.mixup.editor.RecyclerViewTestUtil.checkRecyclerViewContainsLayoutOptions
import com.gerardbradshaw.mixup.editor.RecyclerViewTestUtil.checkRecyclerViewContainsAspectRatioOptions
import com.gerardbradshaw.mixup.editor.RecyclerViewTestUtil.clickRecyclerViewAtPosition
import com.gerardbradshaw.mixup.ui.MainActivity
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized


@RunWith(Enclosed::class)
class EditorFragmentTests {

  // Done!
  @RunWith(AndroidJUnit4::class)
  class InitializationTests {
    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_useAppContext_when_launched() {
      val appContext = InstrumentationRegistry.getInstrumentation().targetContext
      assertEquals("com.gerardbradshaw.mixup", appContext.packageName)
    }

    @Test
    fun should_showLayoutOptions_when_firstEntering() {
      checkRecyclerViewContainsLayoutOptions()
    }

    @Test
    fun should_haveNoDefinedAspectRatio_when_firstEntering() {
      checkCollageHasAspectRatioSetTo(null)
    }

    @Test
    fun should_showOptionsMenu_when_firstEntering() {
      checkOptionsMenuVisibility(true)
    }

    @Test
    fun should_startCollageWithNoBorder_when_firstEntering() {
      checkCollageIsBorderEnabled(false)
    }

    @Test
    fun should_startCollageWith3image2layout_when_firstEntering() {
      checkCollageTypeIs(CollageView3Image2::class.java)
    }
  }

  // Done!
  @RunWith(AndroidJUnit4::class)
  class IOTests {
    lateinit var activityScenario: ActivityScenario<MainActivity>
    lateinit var activity: MainActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
      ApplicationProvider.getApplicationContext<BaseApplication>().replaceDispatchersForTests()

      activityScenario = ActivityScenario.launch(MainActivity::class.java)

      activityScenario.onActivity {
        activity = it
      }
    }

    @Test
    fun should_saveCollageToGallery_when_saveButtonPressed() {
      val initialImageCount = countImagesOnDevice(activity)
      pressOptionsMenuButton(R.id.action_save)
      assertEquals(initialImageCount + 1, countImagesOnDevice(activity))
    }

    @Test
    fun should_shareCollage_when_shareButtonPressed() {
      Intents.init()

      pressOptionsMenuButton(R.id.action_share)

      intended(allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(`is`(Intent.EXTRA_INTENT),
        allOf(hasAction(Intent.ACTION_SEND), hasExtraWithKey(Intent.EXTRA_STREAM)))))

      Intents.release()
    }


  }

  // Done!
  @RunWith(AndroidJUnit4::class)
  class CollageLayoutTests {
    lateinit var activityScenario: ActivityScenario<MainActivity>
    lateinit var activity: MainActivity

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun setup() {
      activityScenario = ActivityScenario.launch(MainActivity::class.java)

      activityScenario.onActivity {
        activity = it
      }
    }

    @Test
    fun should_showLayoutOptions_when_layoutButtonClicked() {
      onView(withId(R.id.button_layout)).perform(click())
      checkRecyclerViewContainsLayoutOptions()
    }

    @Test
    fun should_loadPreviouslySelectedImages_when_layoutChanged() {
      changeAllImages(3, activity)
      clickRecyclerViewAtPosition(3)
      checkAllImagesIsDefault(false)
    }

    @Test
    fun should_notChangeCollageType_when_resetButtonPressed() {
      clickRecyclerViewAtPosition(3)
      pressOptionsMenuButton(R.id.action_reset)
      checkCollageTypeIs(CollageView3Image1::class.java)
    }

    @Test
    fun should_haveDefaultImages_when_resetButtonPressed() {
      changeAllImages(3, activity)
      pressOptionsMenuButton(R.id.action_reset)
      checkAllImagesIsDefault(true)
    }
  }

  // Done!!
  @RunWith(Parameterized::class)
  class ParameterizedLayoutChangeTests(
    private val inputRecyclerPosition: Int,
    private val expectedViewType: Class<AbstractCollageView>) {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_changeLayout_when_newLayoutSelected() {
      clickRecyclerViewAtPosition(inputRecyclerPosition)
      checkCollageTypeIs(expectedViewType)
    }

    companion object {
      @Parameterized.Parameters(name = "layout = {0}")
      @JvmStatic
      fun params(): Collection<Array<Any>> {
        val expectedOutputs = arrayOf<Any>(
          CollageViewVertical::class.java,
          CollageViewHorizontal::class.java,
          CollageView3Image0::class.java,
          CollageView3Image1::class.java,
          CollageView3Image2::class.java,
          CollageView3Image3::class.java,
          CollageViewHorizontal::class.java,
          CollageViewVertical::class.java,
          CollageView4Image0::class.java,
          CollageView4Image1::class.java,
          CollageView4Image2::class.java,
          CollageView4Image3::class.java,
          CollageView4Image4::class.java)

        val inputParams = Array<Any>(expectedOutputs.size) { it }

        return Array(inputParams.size) {
          arrayOf(inputParams[it], expectedOutputs[it])
        }.asList()
      }
    }
  }

  // Done!!
  @RunWith(AndroidJUnit4::class)
  class AspectRatioTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_showAspectRatioOptions_when_aspectRatioButtonClicked() {
      pressOptionsMenuButton(R.id.button_aspect_ratio)
      checkRecyclerViewContainsAspectRatioOptions()
    }
  }

  // Done!!
  @RunWith(Parameterized::class)
  class ParameterizedAspectRatioTests(
    private val inputRecyclerPosition: Int,
    private val expectedOutputRatio: Float) {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_changeAspectRatio_when_newAspectRatioSelected() {
      pressOptionsMenuButton(R.id.button_aspect_ratio)
      clickRecyclerViewAtPosition(inputRecyclerPosition)
      checkCollageHasActualAspectRatio(expectedOutputRatio)
    }

    companion object {
      @Parameterized.Parameters(name = "ratio = {0}")
      @JvmStatic
      fun params(): Collection<Array<Any>> {
        val expectedOutputs = arrayOf<Any>(
          1f,
          16f / 9f,
          9f / 16f,
          10f / 8f,
          8f / 10f,
          7f / 5f,
          5f / 7f,
          4f / 3f,
          3f / 4f,
          5f / 3f,
          3f / 5f,
          3f / 2f,
          2f / 3f)

        val inputParams = Array<Any>(expectedOutputs.size) { it }

        return Array(inputParams.size) {
          arrayOf(inputParams[it], expectedOutputs[it])
        }.asList()
      }
    }
  }


  @RunWith(AndroidJUnit4::class)
  class BorderTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_showBorderOptions_when_borderButtonPressed() {
      TODO()
    }

    @Test
    fun should_ignoreBorderButtonPress_when_alreadyShowingBorderOptions() {
      TODO()
    }

    @Test
    fun should_startWithBorderSwitchOff_when_firstEnteringBorderOptions() {
      TODO()
    }

    @Test
    fun should_enableBorder_when_switchTurnedOnFromOffState() {
      TODO()
    }

    @Test
    fun should_disableBorder_when_switchTurnedOffFromOnState() {
      TODO()
    }

    @Test
    fun should_enableBorderSwitch_when_newBorderColorSelected() {
      TODO()
    }

    @Test
    fun should_changeBorderColor_when_newBorderColorSelected() {
      TODO()
    }
  }


  @RunWith(AndroidJUnit4::class)
  class CollageTests {

    @Rule
    @JvmField
    val asr = ActivityScenarioRule<MainActivity>(MainActivity::class.java)

    @Test
    fun should_openGalleryApp_when_imageClicked() {
      TODO()
    }

    @Test
    fun should_importImageIntoCollage_when_imageSelectedFromGallery() {
      TODO()
    }

  }
}