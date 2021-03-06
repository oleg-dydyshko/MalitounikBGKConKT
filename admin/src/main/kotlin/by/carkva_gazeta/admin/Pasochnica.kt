package by.carkva_gazeta.admin

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import by.carkva_gazeta.admin.databinding.AdminPasochnicaBinding
import by.carkva_gazeta.malitounik.InteractiveScrollView
import by.carkva_gazeta.malitounik.MainActivity
import by.carkva_gazeta.malitounik.SettingsActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class Pasochnica : AppCompatActivity(), View.OnClickListener, DialogPasochnicaFileName.DialogPasochnicaFileNameListener, DialogSaveAsFileExplorer.DialogSaveAsFileExplorerListener, DialogFileExists.DialogFileExistsListener, DialogPasochnicaMkDir.DialogPasochnicaMkDirListener, DialogAddPesny.DialogAddPesnyListiner, InteractiveScrollView.OnScrollChangedCallback {

    private lateinit var k: SharedPreferences
    private lateinit var binding: AdminPasochnicaBinding
    private var resetTollbarJob: Job? = null
    private var fileName = "newFile.html"
    private var history = ArrayList<History>()
    private var positionY = 0
    private var firstTextPosition = ""
    private val textWatcher = object : TextWatcher {
        var editPosition = 0

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            editPosition = start + count
        }

        override fun afterTextChanged(s: Editable?) {
            addHistory(s, editPosition)
            if (history.size > 1) {
                binding.actionBack.visibility = View.VISIBLE
            } else {
                binding.actionBack.visibility = View.GONE
            }
        }
    }

    private fun addHistory(s: Editable?, editPosition: Int) {
        s?.let {
            if (it.toString() != "") {
                if (history.size == 51) history.removeAt(0)
                history.add(History(it.toSpannable(), editPosition))
            }
            if (history.size > 1) {
                binding.actionBack.visibility = View.VISIBLE
            } else {
                binding.actionBack.visibility = View.GONE
            }
        }
    }

    override fun onScroll(t: Int, oldt: Int) {
        positionY = t
        val laneLayout = binding.apisanne.layout
        laneLayout?.let { layout ->
            val textForVertical = binding.apisanne.text.toString().substring(layout.getLineStart(layout.getLineForVertical(positionY)), layout.getLineEnd(layout.getLineForVertical(positionY))).trim()
            if (textForVertical != "") firstTextPosition = textForVertical
        }
    }

    override fun onPause() {
        super.onPause()
        resetTollbarJob?.cancel()
        val prefEditor = k.edit()
        prefEditor.putInt("admin" + fileName + "position", positionY)
        prefEditor.apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        k = getSharedPreferences("biblia", Context.MODE_PRIVATE)
        if (!MainActivity.checkBrightness) {
            val lp = window.attributes
            lp.screenBrightness = MainActivity.brightness.toFloat() / 100
            window.attributes = lp
        }
        super.onCreate(savedInstanceState)
        binding = AdminPasochnicaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apisanne.addTextChangedListener(textWatcher)
        binding.actionBold.setOnClickListener(this)
        binding.actionEm.setOnClickListener(this)
        binding.actionRed.setOnClickListener(this)
        binding.actionP.setOnClickListener(this)
        binding.actionBr.setOnClickListener(this)
        binding.actionBack.setOnClickListener(this)
        binding.scrollView.setOnScrollChangedCallback(this)
        fileName = intent.extras?.getString("fileName", "newFile.html") ?: "newFile.html"
        val text = intent.extras?.getString("text", "") ?: ""
        if (savedInstanceState != null) {
            fileName = savedInstanceState.getString("fileName", "")
            history.clear()
            binding.apisanne.post {
                val textline = savedInstanceState.getString("textLine", "")
                if (textline != "") {
                    val index = binding.apisanne.text.toString().indexOf(textline)
                    val line = binding.apisanne.layout.getLineForOffset(index)
                    val y = binding.apisanne.layout.getLineTop(line)
                    binding.scrollView.scrollY = y
                } else {
                    binding.scrollView.smoothScrollBy(0, positionY)
                }
            }
        } else {
            if (fileName != "newFile.html") {
                getFilePostRequest(fileName)
            } else {
                if (fileName.contains(".htm")) {
                    binding.apisanne.setText(MainActivity.fromHtml(text))
                    binding.actionP.visibility = View.GONE
                    binding.actionBr.visibility = View.GONE
                } else {
                    binding.apisanne.setText(text)
                }
            }

        }
        if (text != "") {
            val gson = Gson()
            val resours = intent.extras?.getString("resours", "") ?: ""
            val title = intent.extras?.getString("title", "") ?: ""
            fileName = if (resours == "") {
                title
            } else {
                "($resours) $title.html"
            }
            if (intent.extras?.getBoolean("exits", false) == false) {
                sendPostRequest(fileName, gson.toJson(text))
            } else {
                getFilePostRequest(fileName)
            }
        }
        positionY = k.getInt("admin" + fileName + "position", 0)
        setTollbarTheme()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("fileName", fileName)
        outState.putString("textLine", firstTextPosition)
    }

    private fun setTollbarTheme() {
        binding.titleToolbar.setOnClickListener {
            fullTextTollbar()
        }
        binding.titleToolbar.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN + 4.toFloat())
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.titleToolbar.text = getString(by.carkva_gazeta.malitounik.R.string.pasochnica)
    }

    private fun fullTextTollbar() {
        val layoutParams = binding.toolbar.layoutParams
        resetTollbarJob?.cancel()
        if (binding.titleToolbar.isSelected) {
            resetTollbar(layoutParams)
        } else {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            binding.titleToolbar.isSingleLine = false
            binding.titleToolbar.isSelected = true
            resetTollbarJob = CoroutineScope(Dispatchers.Main).launch {
                delay(5000)
                resetTollbar(layoutParams)
            }
        }
    }

    private fun resetTollbar(layoutParams: ViewGroup.LayoutParams) {
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            val actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            layoutParams.height = actionBarHeight
        }
        binding.titleToolbar.isSelected = false
        binding.titleToolbar.isSingleLine = true
    }

    override fun onResume() {
        super.onResume()
        setTollbarTheme()
        overridePendingTransition(by.carkva_gazeta.malitounik.R.anim.alphain, by.carkva_gazeta.malitounik.R.anim.alphaout)
        if (k.getBoolean("scrinOn", false)) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onBackPressed() {
        onSupportNavigateUp()
    }

    override fun onDialogSaveAsFile(dir: String, oldFileName: String, fileName: String) {
        getFileIssetPostRequest(dir, oldFileName, fileName)
    }

    override fun setFileName(oldFileName: String, fileName: String) {
        this.fileName = fileName
        saveResult(fileName)
    }

    override fun fileExists(dir: String, oldFileName: String, fileName: String) {
        sendSaveAsPostRequest("$dir/$fileName", oldFileName)
    }

    override fun setDir(oldDir: String) {
        val dialogSaveAsFileExplorer = supportFragmentManager.findFragmentByTag("dialogSaveAsFileExplorer") as? DialogSaveAsFileExplorer
        dialogSaveAsFileExplorer?.mkDir(oldDir)
    }

    override fun addPesny(title: String, pesny: String, fileName: String) {
        sendSaveAsAddNewPesnyPostRequest(title, pesny, fileName)
    }

    private fun sendSaveAsAddNewPesnyPostRequest(title: String, pesny: String, fileName: String) {
        if (MainActivity.isNetworkAvailable()) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressBar2.visibility = View.VISIBLE
                var responseCodeS = 500
                withContext(Dispatchers.IO) {
                    var reqParam = URLEncoder.encode("NewPesny", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")
                    reqParam += "&" + URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(title, "UTF-8")
                    reqParam += "&" + URLEncoder.encode("pesny", "UTF-8") + "=" + URLEncoder.encode(pesny, "UTF-8")
                    reqParam += "&" + URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(fileName, "UTF-8")
                    val mURL = URL("https://carkva-gazeta.by/admin/piasochnica.php")
                    with(mURL.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        val wr = OutputStreamWriter(outputStream)
                        wr.write(reqParam)
                        wr.flush()
                        responseCodeS = responseCode
                    }
                }
                if (responseCodeS == 200) {
                    MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.save))
                } else {
                    MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.error))
                }
                binding.progressBar2.visibility = View.GONE
            }
        }
    }

    private fun getFileIssetPostRequest(dir: String, oldFileName: String, fileName: String) {
        if (MainActivity.isNetworkAvailable()) {
            CoroutineScope(Dispatchers.Main).launch {
                var result = ""
                binding.progressBar2.visibility = View.VISIBLE
                withContext(Dispatchers.IO) {
                    var reqParam = URLEncoder.encode("isset", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")
                    reqParam += "&" + URLEncoder.encode("dir", "UTF-8") + "=" + URLEncoder.encode(dir, "UTF-8")
                    reqParam += "&" + URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(fileName, "UTF-8")
                    val mURL = URL("https://carkva-gazeta.by/admin/piasochnica.php")
                    with(mURL.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        val wr = OutputStreamWriter(outputStream)
                        wr.write(reqParam)
                        wr.flush()
                        val sb = StringBuilder()
                        BufferedReader(InputStreamReader(inputStream)).use {
                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                sb.append(inputLine)
                                inputLine = it.readLine()
                            }
                        }
                        val gson = Gson()
                        val type = object : TypeToken<String>() {}.type
                        result = gson.fromJson(sb.toString(), type)
                    }
                }
                if (result.contains("true")) {
                    val dialogFileExists = DialogFileExists.getInstance(dir, oldFileName, fileName)
                    dialogFileExists.show(supportFragmentManager, "dialogFileExists")
                } else {
                    sendSaveAsPostRequest("$dir/$fileName", oldFileName)
                }
                binding.progressBar2.visibility = View.GONE
            }
        }
    }

    private fun sendSaveAsPostRequest(dirToFile: String, fileName: String) {
        if (MainActivity.isNetworkAvailable()) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressBar2.visibility = View.VISIBLE
                var responseCodeS = 500
                withContext(Dispatchers.IO) {
                    var reqParam = URLEncoder.encode("saveas", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")
                    reqParam += "&" + URLEncoder.encode("dirToFile", "UTF-8") + "=" + URLEncoder.encode(dirToFile, "UTF-8")
                    reqParam += "&" + URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(fileName, "UTF-8")
                    val mURL = URL("https://carkva-gazeta.by/admin/piasochnica.php")
                    with(mURL.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        val wr = OutputStreamWriter(outputStream)
                        wr.write(reqParam)
                        wr.flush()
                        responseCodeS = responseCode
                    }
                }
                if (responseCodeS == 200) {
                    MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.save))
                } else {
                    MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.error))
                }
                binding.progressBar2.visibility = View.GONE
            }
        }
    }

    private fun getFilePostRequest(fileName: String) {
        if (MainActivity.isNetworkAvailable()) {
            CoroutineScope(Dispatchers.Main).launch {
                var result = ""
                binding.progressBar2.visibility = View.VISIBLE
                withContext(Dispatchers.IO) {
                    var reqParam = URLEncoder.encode("get", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")
                    reqParam += "&" + URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(fileName, "UTF-8")
                    val mURL = URL("https://carkva-gazeta.by/admin/piasochnica.php")
                    with(mURL.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        val wr = OutputStreamWriter(outputStream)
                        wr.write(reqParam)
                        wr.flush()
                        val sb = StringBuilder()
                        BufferedReader(InputStreamReader(inputStream)).use {
                            var inputLine = it.readLine()
                            while (inputLine != null) {
                                sb.append(inputLine)
                                inputLine = it.readLine()
                            }
                        }
                        val gson = Gson()
                        val type = object : TypeToken<String>() {}.type
                        result = gson.fromJson(sb.toString(), type)
                    }
                }

                if (fileName.contains(".htm")) {
                    binding.apisanne.setText(MainActivity.fromHtml(result))
                    binding.actionP.visibility = View.GONE
                    binding.actionBr.visibility = View.GONE
                } else {
                    binding.apisanne.setText(result)
                }
                binding.apisanne.post {
                    binding.scrollView.smoothScrollBy(0, positionY)
                }
                binding.progressBar2.visibility = View.GONE
            }
        }
    }

    private fun sendPostRequest(fileName: String, content: String) {
        if (MainActivity.isNetworkAvailable()) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.progressBar2.visibility = View.VISIBLE
                var responseCodeS = 500
                withContext(Dispatchers.IO) {
                    var reqParam = URLEncoder.encode("save", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")
                    reqParam += "&" + URLEncoder.encode("fileName", "UTF-8") + "=" + URLEncoder.encode(fileName, "UTF-8")
                    reqParam += "&" + URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(content, "UTF-8")
                    val mURL = URL("https://carkva-gazeta.by/admin/piasochnica.php")
                    with(mURL.openConnection() as HttpURLConnection) {
                        requestMethod = "POST"
                        val wr = OutputStreamWriter(outputStream)
                        wr.write(reqParam)
                        wr.flush()
                        responseCodeS = responseCode
                    }
                }
                if (responseCodeS == 200) {
                    MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.save))
                } else {
                    MainActivity.toastView(getString(by.carkva_gazeta.malitounik.R.string.error))
                }
                binding.progressBar2.visibility = View.GONE
            }
        }
    }

    private fun clearColor(text: String): String {
        var result = text
        var run = true
        var position = 0
        while (run) {
            val t1 = result.indexOf("<font color=\"#d00505\">", position)
            val t2 = result.indexOf("</font>", t1)
            if (t1 != -1 && t2 != -1) {
                var subText = result.substring(t1 + 22, t2)
                val oldSubText = result.substring(t1, t2 + 7)
                subText = subText.replace("\n", "")
                subText = subText.replace("<br>", "")
                subText = subText.replace("<p>", "").trim()
                if (subText.isEmpty()) {
                    var oldSubText2 = oldSubText.replace("<font color=\"#d00505\">", "")
                    oldSubText2 = oldSubText2.replace("</font>", "")
                    result = result.replace(oldSubText, oldSubText2)
                }
            } else {
                run = false
            }
            position = t1 + 1
        }
        run = true
        position = 0
        while (run) {
            val t1 = result.indexOf("</font>", position)
            val t2 = result.indexOf("<font color=\"#d00505\">", t1)
            if (t1 != -1 && t2 != -1) {
                var subText = result.substring(t1 + 7, t2)
                val oldSubText = result.substring(t1, t2 + 22)
                subText = subText.replace("\n", "")
                subText = subText.replace("<br>", "")
                subText = subText.replace("<p>", "").trim()
                if (subText.isEmpty()) {
                    var oldSubText2 = oldSubText.replace("<font color=\"#d00505\">", "")
                    oldSubText2 = oldSubText2.replace("</font>", "")
                    result = result.replace(oldSubText, oldSubText2)
                }
            } else {
                run = false
            }
            position = t1 + 1
        }
        return result
    }

    private fun clearBold(text: String): String {
        var result = text
        var run = true
        var position = 0
        while (run) {
            val t1 = result.indexOf("<strong>", position)
            val t2 = result.indexOf("</strong>", t1)
            if (t1 != -1 && t2 != -1) {
                var subText = result.substring(t1 + 8, t2)
                val oldSubText = result.substring(t1, t2 + 9)
                subText = subText.replace("\n", "")
                subText = subText.replace("<br>", "")
                subText = subText.replace("<p>", "").trim()
                if (subText.isEmpty()) {
                    var oldSubText2 = oldSubText.replace("<strong>", "")
                    oldSubText2 = oldSubText2.replace("</strong>", "")
                    result = result.replace(oldSubText, oldSubText2)
                }
            } else {
                run = false
            }
            position = t1 + 1
        }
        run = true
        position = 0
        while (run) {
            val t1 = result.indexOf("</strong>", position)
            val t2 = result.indexOf("<strong>", t1)
            if (t1 != -1 && t2 != -1) {
                var subText = result.substring(t1 + 9, t2)
                val oldSubText = result.substring(t1, t2 + 8)
                subText = subText.replace("\n", "")
                subText = subText.replace("<br>", "")
                subText = subText.replace("<p>", "").trim()
                if (subText.isEmpty()) {
                    var oldSubText2 = oldSubText.replace("<strong>", "")
                    oldSubText2 = oldSubText2.replace("</strong>", "")
                    result = result.replace(oldSubText, oldSubText2)
                }
            } else {
                run = false
            }
            position = t1 + 1
        }
        return result
    }

    private fun clearEm(text: String): String {
        var result = text
        var run = true
        var position = 0
        while (run) {
            val t1 = result.indexOf("<em>", position)
            val t2 = result.indexOf("</em>", t1)
            if (t1 != -1 && t2 != -1) {
                var subText = result.substring(t1 + 4, t2)
                val oldSubText = result.substring(t1, t2 + 5)
                subText = subText.replace("\n", "")
                subText = subText.replace("<br>", "")
                subText = subText.replace("<p>", "").trim()
                if (subText.isEmpty()) {
                    var oldSubText2 = oldSubText.replace("<em>", "")
                    oldSubText2 = oldSubText2.replace("</em>", "")
                    result = result.replace(oldSubText, oldSubText2)
                }
            } else {
                run = false
            }
            position = t1 + 1
        }
        run = true
        position = 0
        while (run) {
            val t1 = result.indexOf("</em>", position)
            val t2 = result.indexOf("<em>", t1)
            if (t1 != -1 && t2 != -1) {
                var subText = result.substring(t1 + 5, t2)
                val oldSubText = result.substring(t1, t2 + 4)
                subText = subText.replace("\n", "")
                subText = subText.replace("<br>", "")
                subText = subText.replace("<p>", "").trim()
                if (subText.isEmpty()) {
                    var oldSubText2 = oldSubText.replace("<em>", "")
                    oldSubText2 = oldSubText2.replace("</em>", "")
                    result = result.replace(oldSubText, oldSubText2)
                }
            } else {
                run = false
            }
            position = t1 + 1
        }
        return result
    }

    private fun clearHtml(text: String): String {
        var result = text
        val t1 = result.indexOf("<p")
        if (t1 != -1) {
            val t2 = result.indexOf(">")
            val subString = result.substring(t1, t2 + 1)
            var stringres = result.replace(subString, "")
            stringres = stringres.replace("</p>", "<br>")
            stringres = stringres.replace("<span", "<font")
            stringres = stringres.replace("</span>", "</font>")
            stringres = stringres.replace("style=\"color:#D00505;\"", "color=\"#d00505\"")
            stringres = stringres.replace("<i>", "<em>")
            stringres = stringres.replace("</i>", "</em>")
            stringres = stringres.replace("<b>", "<strong>")
            stringres = stringres.replace("</b>", "</strong>")
            stringres = stringres.replace("<u>", "")
            stringres = stringres.replace("</u>", "")
            val t3 = stringres.lastIndexOf("<br>")
            result = stringres.substring(0, t3)
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_save) {
            if (fileName == "newFile.html") {
                val dialogPasochnicaFileName = DialogPasochnicaFileName.getInstance("newFile.html")
                dialogPasochnicaFileName.show(supportFragmentManager, "dialogPasochnicaFileName")
            } else {
                saveResult(fileName)
            }
        }
        if (id == R.id.action_save_as) {
            val dialogSaveAsFileExplorer = DialogSaveAsFileExplorer.getInstance(fileName)
            dialogSaveAsFileExplorer.show(supportFragmentManager, "dialogSaveAsFileExplorer")
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveResult(fileName: String) {
        val text = binding.apisanne.text
        val gson = Gson()
        if (fileName.contains(".htm")) {
            text?.let {
                var result = MainActivity.toHtml(it)
                result = StringEscapeUtils.unescapeHtml4(result)
                result = clearHtml(result)
                result = clearColor(result)
                result = clearEm(result)
                result = clearBold(result)
                result = clearEm(result)
                result = clearColor(result)
                result = clearBold(result)
                result = clearEm(result)
                if (!result.contains("<!DOCTYPE HTML>")) result = "<!DOCTYPE HTML>$result"
                sendPostRequest(fileName, gson.toJson(result))
            }
        } else {
            sendPostRequest(fileName, gson.toJson(text.toString()))
        }
    }

    override fun onClick(v: View?) {
        val id = v?.id ?: 0
        if (id == R.id.action_back) {
            binding.apisanne.removeTextChangedListener(textWatcher)
            if (history.size > 1) {
                binding.apisanne.setText(history[history.size - 2].spannable)
                binding.apisanne.setSelection(history[history.size - 2].editPosition)
                history.removeAt(history.size - 1)
            }
            if (history.size > 1) {
                binding.actionBack.visibility = View.VISIBLE
            } else {
                binding.actionBack.visibility = View.GONE
            }
            binding.apisanne.addTextChangedListener(textWatcher)
        }
        if (id == R.id.action_bold) {
            val startSelect = binding.apisanne.selectionStart
            val endSelect = binding.apisanne.selectionEnd
            if (fileName.contains(".htm")) {
                val text = binding.apisanne.text
                text?.let { editable ->
                    val subtext = editable.getSpans(startSelect, endSelect, StyleSpan(Typeface.BOLD)::class.java)
                    var check = false
                    subtext.forEach {
                        if (it.style == Typeface.BOLD) {
                            check = true
                            editable.removeSpan(it)
                        }
                    }
                    if (!check) editable.setSpan(StyleSpan(Typeface.BOLD), startSelect, endSelect, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                val text = binding.apisanne.text.toString()
                val build = with(StringBuilder()) {
                    append(text.substring(0, startSelect))
                    append("<strong>")
                    append(text.substring(startSelect, endSelect))
                    append("</strong>")
                    append(text.substring(endSelect))
                    toString()
                }
                binding.apisanne.setText(build)
                binding.apisanne.setSelection(endSelect + 17)
            }
            addHistory(binding.apisanne.text, binding.apisanne.selectionEnd)
        }
        if (id == R.id.action_em) {
            val startSelect = binding.apisanne.selectionStart
            val endSelect = binding.apisanne.selectionEnd
            if (fileName.contains(".htm")) {
                val text = binding.apisanne.text
                text?.let { editable ->
                    val subtext = editable.getSpans(startSelect, endSelect, StyleSpan(Typeface.ITALIC)::class.java)
                    var check = false
                    subtext.forEach {
                        if (it.style == Typeface.ITALIC) {
                            check = true
                            editable.removeSpan(it)
                        }
                    }
                    if (!check) editable.setSpan(StyleSpan(Typeface.ITALIC), startSelect, endSelect, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                val text = binding.apisanne.text.toString()
                val build = with(StringBuilder()) {
                    append(text.substring(0, startSelect))
                    append("<em>")
                    append(text.substring(startSelect, endSelect))
                    append("</em>")
                    append(text.substring(endSelect))
                    toString()
                }
                binding.apisanne.setText(build)
                binding.apisanne.setSelection(endSelect + 9)
            }
            addHistory(binding.apisanne.text, binding.apisanne.selectionEnd)
        }
        if (id == R.id.action_red) {
            val startSelect = binding.apisanne.selectionStart
            val endSelect = binding.apisanne.selectionEnd
            if (fileName.contains(".htm")) {
                val text = binding.apisanne.text
                text?.let { editable ->
                    val subtext = editable.getSpans(startSelect, endSelect, ForegroundColorSpan::class.java)
                    var check = false
                    subtext.forEach {
                        if (it.foregroundColor == ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary)) {
                            check = true
                            editable.removeSpan(it)
                        }
                    }
                    if (!check) editable.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, by.carkva_gazeta.malitounik.R.color.colorPrimary)), startSelect, endSelect, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                val text = binding.apisanne.text.toString()
                val build = with(StringBuilder()) {
                    append(text.substring(0, startSelect))
                    append("<font color=\"#d00505\">")
                    append(text.substring(startSelect, endSelect))
                    append("</font>")
                    append(text.substring(endSelect))
                    toString()
                }
                binding.apisanne.setText(build)
                binding.apisanne.setSelection(endSelect + 29)
            }
            addHistory(binding.apisanne.text, binding.apisanne.selectionEnd)
        }
        if (id == R.id.action_br) {
            val endSelect = binding.apisanne.selectionEnd
            val text = binding.apisanne.text.toString()
            val build = with(StringBuilder()) {
                append(text.substring(0, endSelect))
                append("<br>")
                append(text.substring(endSelect))
                toString()
            }
            binding.apisanne.setText(build)
            binding.apisanne.setSelection(endSelect + 4)
            addHistory(binding.apisanne.text, binding.apisanne.selectionEnd)
        }
        if (id == R.id.action_p) {
            val endSelect = binding.apisanne.selectionEnd
            val text = binding.apisanne.text.toString()
            val build = with(StringBuilder()) {
                append(text.substring(0, endSelect))
                append("<p>")
                append(text.substring(endSelect))
                toString()
            }
            binding.apisanne.setText(build)
            binding.apisanne.setSelection(endSelect + 3)
            addHistory(binding.apisanne.text, binding.apisanne.selectionEnd)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val infl = menuInflater
        infl.inflate(R.menu.edit_piasochnica, menu)
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val spanString = SpannableString(menu.getItem(i).title.toString())
            val end = spanString.length
            spanString.setSpan(AbsoluteSizeSpan(SettingsActivity.GET_FONT_SIZE_MIN.toInt(), true), 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            item.title = spanString
        }
        return true
    }

    private data class History(val spannable: Spannable, val editPosition: Int)
}