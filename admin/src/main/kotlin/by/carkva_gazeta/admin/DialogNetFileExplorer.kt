package by.carkva_gazeta.admin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import by.carkva_gazeta.admin.databinding.AdminSimpleListItemBinding
import by.carkva_gazeta.malitounik.MainActivity
import by.carkva_gazeta.malitounik.SettingsActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class DialogNetFileExplorer : DialogFragment() {

    private lateinit var alert: AlertDialog
    private var mListener: DialogNetFileExplorerListener? = null
    private lateinit var adapter: TitleListAdaprer
    private val fileList = ArrayList<MyNetFile>()
    private var dir = ""

    internal interface DialogNetFileExplorerListener {
        fun onDialogNetFile(dirToFile: String, fileName: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is Activity) {
            mListener = try {
                context as DialogNetFileExplorerListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$context must implement DialogNetFileExplorerListener")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            val builder = AlertDialog.Builder(it, by.carkva_gazeta.malitounik.R.style.AlertDialogTheme)
            val linear = LinearLayout(it)
            linear.orientation = LinearLayout.VERTICAL
            val textViewZaglavie = TextView(it)
            textViewZaglavie.setBackgroundColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorPrimary))
            val density = resources.displayMetrics.density
            val realpadding = (10 * density).toInt()
            textViewZaglavie.setPadding(realpadding, realpadding, realpadding, realpadding)
            textViewZaglavie.text = "ВЫБЕРЫЦЕ ФАЙЛ"
            textViewZaglavie.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            textViewZaglavie.typeface = MainActivity.createFont(it,  Typeface.BOLD)
            textViewZaglavie.setTextColor(ContextCompat.getColor(it, by.carkva_gazeta.malitounik.R.color.colorWhite))
            linear.addView(textViewZaglavie)
            val listViewCompat = ListView(it)
            listViewCompat.selector = ContextCompat.getDrawable(it, by.carkva_gazeta.malitounik.R.drawable.selector_default)
            adapter = TitleListAdaprer(it)
            listViewCompat.adapter = adapter
            linear.addView(listViewCompat)
            builder.setView(linear)
            getDirListRequest("")
            listViewCompat.setOnItemClickListener { _, _, position, _ ->
                when (fileList[position].resources) {
                    R.drawable.directory_up -> {
                        val t1 = dir.lastIndexOf("/")
                        dir = dir.substring(0, t1)
                        getDirListRequest(dir)
                    }
                    R.drawable.directory_icon -> {
                        dir = dir + "/" + fileList[position].title
                        getDirListRequest(dir)
                    }
                    else -> {
                        mListener?.onDialogNetFile(dir + "/" + fileList[position].title, fileList[position].title)
                        dialog?.cancel()
                    }
                }
            }
            builder.setPositiveButton(getString(by.carkva_gazeta.malitounik.R.string.cansel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alert = builder.create()
        }
        return alert
    }

    private fun getDirListRequest(dir: String) {
        activity?.let { activity ->
            if (MainActivity.isNetworkAvailable(activity)) {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        var reqParam = URLEncoder.encode("list", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")
                        reqParam += "&" + URLEncoder.encode("dir", "UTF-8") + "=" + URLEncoder.encode(dir, "UTF-8")
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
                            val result = sb.toString()
                            fileList.clear()
                            val temp = ArrayList<MyNetFile>()
                            if (result != "null") {
                                val gson = Gson()
                                val type = object : TypeToken<ArrayList<ArrayList<String>>>() {}.type
                                val arrayList = ArrayList<ArrayList<String>>()
                                arrayList.addAll(gson.fromJson(result, type))
                                arrayList.forEach {
                                    if (it[0].contains("dir")) {
                                        if (it[1] == "..") temp.add(MyNetFile(R.drawable.directory_up, it[1].replace("..", "Верх")))
                                        else temp.add(MyNetFile(R.drawable.directory_icon, it[1]))
                                    } else {
                                        if (it[1].contains(".htm")) {
                                            temp.add(MyNetFile(R.drawable.file_html_icon, it[1]))
                                        } else {
                                            temp.add(MyNetFile(R.drawable.file_txt_icon, it[1]))
                                        }
                                    }
                                }
                                fileList.addAll(temp)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private inner class TitleListAdaprer(private val mContext: Activity) : ArrayAdapter<MyNetFile>(mContext, R.layout.admin_simple_list_item, fileList) {
        override fun getView(position: Int, mView: View?, parent: ViewGroup): View {
            val rootView: View
            val viewHolder: ViewHolder
            if (mView == null) {
                val binding = AdminSimpleListItemBinding.inflate(LayoutInflater.from(context), parent, false)
                rootView = binding.root
                viewHolder = ViewHolder(binding.label)
                rootView.tag = viewHolder
            } else {
                rootView = mView
                viewHolder = rootView.tag as ViewHolder
            }
            viewHolder.text.text = fileList[position].title
            viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SettingsActivity.GET_FONT_SIZE_MIN)
            val image = ContextCompat.getDrawable(mContext, fileList[position].resources)
            val density = resources.displayMetrics.density.toInt()
            image?.setBounds(0, 0, 48 * density, 48 * density)
            viewHolder.text.setCompoundDrawables(image, null, null, null)
            return rootView
        }

    }

    private class ViewHolder(var text: TextView)

    private data class MyNetFile(val resources: Int, val title: String)
}