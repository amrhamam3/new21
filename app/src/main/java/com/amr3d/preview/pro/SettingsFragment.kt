package com.amr3d.preview.pro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view  = inflater.inflate(R.layout.fragment_settings, container, false)
        val prefs = requireContext().getSharedPreferences("amr3d_prefs", Context.MODE_PRIVATE)
        val ctx   = requireContext()

        // ══ وحدة القياس ══
        val unitGroup = view.findViewById<RadioGroup>(R.id.unitGroup)
        when (prefs.getString("unit", "MM")) {
            "MM"   -> unitGroup.check(R.id.radioMM)
            "CM"   -> unitGroup.check(R.id.radioCM)
            "INCH" -> unitGroup.check(R.id.radioInch)
        }
        unitGroup.setOnCheckedChangeListener { _, id ->
            prefs.edit().putString("unit", when (id) {
                R.id.radioMM   -> "MM"
                R.id.radioCM   -> "CM"
                R.id.radioInch -> "INCH"
                else -> "MM"
            }).apply()
        }

        // ══ تغيير الاسم ══
        view.findViewById<Button>(R.id.btnChangeName).setOnClickListener {
            val input = EditText(ctx).apply {
                hint = "اسمك الجديد"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                setTextColor(0xFFF2F3F5.toInt())
                setHintTextColor(0xFF9CA3AF.toInt())
                setPadding(40, 24, 40, 24)
                textSize = 16f
                val saved = MainActivity.getUserName(ctx)
                if (saved.isNotEmpty()) setText(saved)
            }
            AlertDialog.Builder(ctx)
                .setTitle("👤  تغيير الاسم")
                .setView(input)
                .setPositiveButton("حفظ") { _, _ ->
                    val name = input.text.toString().trim().ifEmpty { "صديقي" }
                    MainActivity.saveUserName(ctx, name)
                    Toast.makeText(ctx, "✅ تم الحفظ: $name", Toast.LENGTH_SHORT).show()
                    refreshVersionText(view)
                }
                .setNegativeButton("إلغاء", null).show()
        }

        // ══ جودة العرض ══
        val qualityGroup = view.findViewById<RadioGroup>(R.id.qualityGroup)
        when (prefs.getString("quality", "HIGH")) {
            "HIGH"   -> qualityGroup.check(R.id.radioHigh)
            "MEDIUM" -> qualityGroup.check(R.id.radioMedium)
            "LOW"    -> qualityGroup.check(R.id.radioLow)
        }
        qualityGroup.setOnCheckedChangeListener { _, id ->
            prefs.edit().putString("quality", when (id) {
                R.id.radioHigh   -> "HIGH"
                R.id.radioMedium -> "MEDIUM"
                R.id.radioLow    -> "LOW"
                else -> "HIGH"
            }).apply()
            Toast.makeText(ctx, "سيُطبَّق عند فتح الملف التالي", Toast.LENGTH_SHORT).show()
        }

        // ══ الصوت ══
        val soundSwitch = view.findViewById<Switch>(R.id.switchSound)
        soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        soundSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("sound_enabled", checked).apply()
        }

        // ══ الانيميشن ══
        val animSwitch = view.findViewById<Switch>(R.id.switchAnim)
        animSwitch.isChecked = prefs.getBoolean("anim_enabled", true)
        animSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("anim_enabled", checked).apply()
        }

        // ══ تواصل ══
        view.findViewById<Button>(R.id.btnContactWA).setOnClickListener {
            val phone = "201009172167"
            val msg = Uri.encode("مرحبًا، عندي استفسار بخصوص تطبيق Amr3D Preview")
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("whatsapp://send?phone=$phone&text=$msg"))
                    .apply { setPackage("com.whatsapp") })
            } catch (_: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=$msg")))
            }
        }

        // ══ مسح التاريخ ══
        view.findViewById<Button>(R.id.btnClearHistorySettings).setOnClickListener {
            AlertDialog.Builder(ctx)
                .setTitle("🗑️ مسح التاريخ")
                .setMessage("هل تريد مسح كل سجل الملفات؟")
                .setPositiveButton("مسح") { _, _ ->
                    HistoryFragment.clearHistory(ctx)
                    Toast.makeText(ctx, "✅ تم المسح", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("إلغاء", null).show()
        }

        refreshVersionText(view)
        return view
    }

    private fun refreshVersionText(view: View) {
        val name = MainActivity.getUserName(requireContext())
        val greeting = if (name.isNotEmpty()) "مرحباً $name 👋\n\n" else ""
        view.findViewById<TextView>(R.id.tvVersion).text =
            "${greeting}🎮  Amr3D Preview Pro\nالإصدار 7.0\nAmr Hamam 3D © 2026"
    }
}
