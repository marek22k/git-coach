package com.vishal2376.gitcoach

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.vishal2376.gitcoach.databinding.ActivityMainBinding
import com.vishal2376.gitcoach.utils.Constants
import com.vishal2376.gitcoach.utils.Constants.shareMessage
import com.vishal2376.gitcoach.utils.LoadSettings
import com.vishal2376.gitcoach.utils.ReminderManager
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationSwitch: SwitchMaterial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //load settings
        LoadSettings.loadTheme(this)
        val checkNotificationSwitch = LoadSettings.checkNotificationSwitch(this)
        val notificationTime = LoadSettings.getNotificationTime(this)

        //create new notification channel
        createNotificationChannel(this)

        // force dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appBarLayout = binding.appBarLayout

        //update app version in nav drawer
        val appVersion = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.tvAppVersion)
        appVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

        binding.ivNavMenu.setOnClickListener {
            handleNavDrawer()
        }

        notificationSwitch =
            binding.navView.getHeaderView(0).findViewById(R.id.swNotification)

        // load default value of switch
        notificationSwitch.isChecked = checkNotificationSwitch
        notificationSwitch.text = getString(R.string.daily_notification, notificationTime)

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTimePickerDialog()
            } else {
                ReminderManager.stopReminder(this)
                Toast.makeText(this, "Notification Disabled", Toast.LENGTH_SHORT).show()
            }

            getSharedPreferences(Constants.NOTIFICATION, MODE_PRIVATE).edit()
                .putBoolean(Constants.NOTIFICATION_SWITCH, isChecked).apply()

        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.itemReportBug -> {
                    reportBug()
                }

                R.id.itemSuggestions -> {
                    getSuggestions()
                }

                R.id.itemRateUs -> {
                    rateUs()
                }

                R.id.itemShareApp -> {
                    shareApp()
                }

                R.id.itemSourceCode -> {
                    sourceCode()
                }

                R.id.itemDeveloper -> {
                    developerProfile()
                }
            }
            true
        }

        binding.ivFontSize.setOnClickListener {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigateUp()
            navController.navigate(R.id.fontSettingFragment)
        }

        binding.ivLanguage.setOnClickListener {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigateUp()
            navController.navigate(R.id.settingsFragment)
        }

    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, { _, hourOfDay, minute ->
                val reminderTime =
                    String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)

                //store reminder time
                getSharedPreferences(Constants.NOTIFICATION, MODE_PRIVATE).edit()
                    .putString(Constants.NOTIFICATION_TIME, reminderTime).apply()

                ReminderManager.stopReminder(this)
                ReminderManager.startReminder(this, reminderTime)

                notificationSwitch.text = getString(R.string.daily_notification, reminderTime)

                Toast.makeText(this, "Notification Enabled", Toast.LENGTH_SHORT).show()
            }, currentHour, currentMinute, false
        )

        timePickerDialog.show()
    }

    private fun handleNavDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareMessage += "https://play.google.com/store/apps/details?id=" + applicationContext.packageName + "\n\n"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Git Coach")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "Share This App"))
    }

    private fun reportBug() {
        val subject = "Git Coach: Report Bug"
        val uriBuilder = StringBuilder("mailto:" + Uri.encode(Constants.email))
        uriBuilder.append("?subject=" + Uri.encode(subject))
        val uriString = uriBuilder.toString()

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriString))
        startActivity(Intent.createChooser(intent, "Report Bug"))
    }

    private fun getSuggestions() {

        val subject = "Git Coach: Suggestions"
        val uriBuilder = StringBuilder("mailto:" + Uri.encode(Constants.email))
        uriBuilder.append("?subject=" + Uri.encode(subject))
        val uriString = uriBuilder.toString()

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriString))
        startActivity(Intent.createChooser(intent, "Send Suggestions"))
    }

    private fun rateUs() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://play.google.com/store/apps/details?id=" + applicationContext.packageName)
        )
        startActivity(intent)

    }

    private fun sourceCode() {
        val intent = Intent(
            Intent.ACTION_VIEW, Uri.parse(Constants.GITHUB_LINK)
        )
        startActivity(intent)

    }

    private fun developerProfile() {
        val intent = Intent(
            Intent.ACTION_VIEW, Uri.parse(Constants.LINKEDIN_LINK)
        )
        startActivity(intent)

    }

    companion object {
        lateinit var appBarLayout: ConstraintLayout
    }
}