package com.example.foodman



import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment


class AlarmFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_alarm, container, false)
        val alarmContainer = view.findViewById<LinearLayout>(R.id.alarm_container)

        val alarms = NotificationHistoryManager.getAllAlarms(requireContext())
        if (alarms.isEmpty()) {
            val text = TextView(requireContext())
            text.text = "알림 내역이 없습니다."
            text.textSize = 16f
            alarmContainer.addView(text)
        } else {
            alarms.forEach { alarm ->
                val card = inflater.inflate(R.layout.item_alarm, alarmContainer, false)

                card.findViewById<TextView>(R.id.text_food_name).text = alarm.foodName
                card.findViewById<TextView>(R.id.text_expiration).text = alarm.expiration
                (card as ViewGroup).getChildAt(0)?.let { // 첫 TextView에 메시지 넣기
                    if (it is TextView) it.text = alarm.message
                }
                alarmContainer.addView(card)
            }
        }
        return view
    }
}
