package ir.awlrhm.modules.extentions

import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import ir.awlrhm.modules.enums.MessageStatus
import ir.awlrhm.modules.security.CheckEmulatorUtil
import ir.awlrhm.modules.security.CheckReverseEngineeringToolsUtil
import ir.awlrhm.modules.security.CheckRootUtil
import ir.awlrhm.modules.security.CloneApp
import ir.awlrhm.modules.utils.calendar.PersianCalendar
import ir.awlrhm.modules.view.ActionDialog
import ir.awlrhm.modules.view.datePicker.CalendarActionListener
import ir.awlrhm.modules.view.datePicker.PersianDatePickerDialog
import ir.awlrhm.modules.view.downloadVersion.DownloadNewVersion
import ir.awlrhm.modules.view.downloadVersion.OnDownloadListener
import ir.awlrhm.modules.view.progress.ProgressDialog
import ir.awrhm.module.TimeDialogFragment
import ir.awrhm.module.utilities.setOnTime24PickedListener
import ir.awrhm.modules.R
import kotlin.system.exitProcess

fun FragmentActivity.showActionDialog(
    title: String = getString(R.string.warning),
    message: String,
    positive: ()-> Unit
){
    ActionDialog.Builder()
        .setTitle(title)
        .setDescription(message)
        .setPositive(getString(R.string.ok)){
            positive.invoke()
        }
        .setNegative(getString(R.string.no)){}
        .build()
        .show(supportFragmentManager, ActionDialog.TAG)
}


fun FragmentActivity.downloadNewVersion(
    downloadUrl: String?,
    listener: OnDownloadListener
) {

    val progressDialog = ProgressDialog.Builder().build()

    DownloadNewVersion(
        this,
        downloadUrl ?: "",
        object : DownloadNewVersion.OnDownload {

            override fun onStart() {
                progressDialog.show(supportFragmentManager, ProgressDialog.TAG)
            }

            override fun onProgress(progress: Int) {
                if (!progressDialog.showsDialog)
                    progressDialog.show(supportFragmentManager, ProgressDialog.TAG)
                progressDialog.setProgress(progress)
                if (progress == 100 && progressDialog.showsDialog) {
                    progressDialog.dismiss()
                    listener.onSuccess()
                }
            }

            override fun onFailed(error: String?) {
                if (progressDialog.showsDialog) {
                    progressDialog.dismiss()
                    listener.onFailed()
                }
            }
        }
    ).execute()
}

fun FragmentActivity.checkSecurity(callback: ()->Unit) {
    val isRooted = CheckRootUtil().isDeviceRooted
    val isEmulator = CheckEmulatorUtil().isEmulator(this)
    val isExistReverseEngTools =
        CheckReverseEngineeringToolsUtil().isExistTools(this)
    val isCloneAppInstalled = CloneApp().isInstalled(this)

    var isSecure = false
    var title = ""
    var message = ""
    when {
        isEmulator -> {
            title = getString(R.string.emulator_detection)
            message = getString(R.string.app_not_run_on_emulator)
        }
        isRooted -> {
            title = getString(R.string.root_detection)
            message = getString(R.string.your_device_is_root)
        }
        isExistReverseEngTools -> {
            title = getString(R.string.reverse_engineering_tools_detection)
            message = getString(R.string.is_exist_reverse_engineering_tools)
        }
        isCloneAppInstalled -> {
            title = getString(R.string.parallel_space_app_exist)
            message = getString(R.string.is_exist_parallel_space_app)
        }
        else -> isSecure = true
    }
    if(isSecure)
        callback.invoke()
    else
        ActionDialog.Builder()
            .setAction(MessageStatus.ERROR)
            .setTitle(if (title.isEmpty()) getString(R.string.error) else title)
            .setDescription(if (message.isEmpty()) getString(R.string.security_problem) else message)
            .setNegative(getString(R.string.ok)) {
                exitProcess(0)
            }
            .build()
            .show(supportFragmentManager, ActionDialog.TAG)
}


fun FragmentActivity.showDateDialog(
    callback: (String) -> Unit
) {
    PersianDatePickerDialog(this)
        .setPositiveButtonString(getString(R.string.ok))
        .setNegativeButton(getString(R.string.cancel))
        .setTodayButton(getString(R.string.today))
        .setTodayButtonVisible(true)
        .setMinYear(1350)
        .setMaxYear(PersianDatePickerDialog.THIS_YEAR)
//        .setInitDate(initDate)
        .setActionTextColor(Color.BLUE)
        .setTypeFace(ResourcesCompat.getFont(this, R.font.iran_sans_mobile))
        .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
        .setShowInBottomSheet(true)
        .setListener(object : CalendarActionListener {
            override fun onDateSelected(persianCalendar: PersianCalendar) {
                callback.invoke(persianCalendar.persianYear.toString() + "/" + persianCalendar.persianMonth + "/" + persianCalendar.persianDay)
            }
            override fun onDismissed() {}
        }).show()
}


fun FragmentActivity.showTimePickerDialog(
    callback: (String) -> Unit
){
    val timePicker = TimeDialogFragment.newInstance()
    timePicker.setInitialTime24(12, 0)
    timePicker.setOnTime24PickedListener {time24 ->
        callback.invoke("${time24.hour} : ${time24.minute}")
    }
    timePicker.show(supportFragmentManager, "TimeDialog")
}

fun FragmentActivity.showError(message: String?){
    message?.let {
        ActionDialog.Builder()
            .setDescription(it)
            .setNegative(getString(R.string.understand)){}
            .build()
            .show(supportFragmentManager, ActionDialog.TAG)
    }?: kotlin.run {
        yToast(
            getString(R.string.error_operation),
            MessageStatus.ERROR
        )
    }
}

fun FragmentActivity.showOfflineMessage(){
    ActionDialog.Builder()
        .setAction(MessageStatus.ERROR)
        .setTitle(getString(R.string.offline_mode))
        .setDescription(getString(R.string.connect_to_internet_update))
        .setNegative(
            getString(R.string.understand)
        ){}
        .build()
        .show(supportFragmentManager, ActionDialog.TAG)
}