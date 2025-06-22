package com.example.foodman



import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 플로팅 버튼 클릭 리스너
        view.findViewById<ImageView>(R.id.fab_add).setOnClickListener {
            showAddPopup()
        }

        // 냉장고/선반 목록 동적 생성
        loadFridgesFromFirestore(view)

        return view
    }

    private fun loadFridgesFromFirestore(root: View) {
        FridgeRepository.getAllFridges { fridgeList ->
            val container = root.findViewById<LinearLayout>(R.id.container_layout)
            container.removeAllViews()
            fridgeList.forEach { (fridgeId, fridge) ->
                addItemCard(root, fridgeId, fridge.name, fridge.type)
            }
        }
    }

    private fun showAddPopup() {
        val context = requireContext()
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialog.findViewById<LinearLayout>(R.id.add_refri_card).setOnClickListener {
            dialog.dismiss()
            showNamePopup("refrigerator")
        }

        dialog.findViewById<LinearLayout>(R.id.add_shelf_card).setOnClickListener {
            dialog.dismiss()
            showNamePopup("shelf")
        }

        dialog.show()
    }

    private fun showNamePopup(type: String) {
        val context = requireContext()
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_name_popup)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        val editText = dialog.findViewById<EditText>(R.id.edit_name)
        val textCount = dialog.findViewById<TextView>(R.id.text_length)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textCount.text = "${s?.length ?: 0}/12"
            }
            override fun afterTextChanged(s: Editable?) {
                if (s?.length ?: 0 > 12) {
                    s?.delete(12, s.length)
                }
            }
        })

        dialog.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            val name = editText.text.toString()
            if (name.isNotEmpty()) {
                val fridge = Fridge(name = name, type = type)
                FridgeRepository.addFridge(fridge) { success ->
                    if (success) {
                        // 저장 후 목록 새로고침
                        loadFridgesFromFirestore(requireView())
                        Toast.makeText(context, "$type 추가됨", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(context, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addItemCard(root: View, fridgeId: String, name: String, type: String) {
        val inflater = LayoutInflater.from(requireContext())
        val cardView = inflater.inflate(R.layout.item_card, null)

        cardView.findViewById<TextView>(R.id.title).text = name

        val cardLayout = cardView.findViewById<LinearLayout>(R.id.item_card_layout)
        val iconView = cardLayout.getChildAt(0) as ImageView
        iconView.setImageResource(
            if (type == "refrigerator") R.drawable.add_refri else R.drawable.add_shelf
        )

        cardView.setOnClickListener {
            val intent = Intent(requireContext(), FridgeDetailActivity::class.java)
            intent.putExtra("title", name)
            intent.putExtra("fridgeId", fridgeId)
            startActivity(intent)
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }

        root.findViewById<LinearLayout>(R.id.container_layout).addView(cardView, layoutParams)
    }
}
