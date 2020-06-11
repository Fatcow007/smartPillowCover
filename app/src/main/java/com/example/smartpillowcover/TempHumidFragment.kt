package com.example.smartpillowcover



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class TempHumidFragment : MyFragment() {

    lateinit var temphumidDetailButton : Button
    lateinit var temphumidAverageTempTextView : TextView
    lateinit var temphumidAverageHumidTextView : TextView
    lateinit var temphumidDiscomfortTextView : TextView
    lateinit var temphumidRecommendTextView : TextView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_temphumid, container, false)
    }

    override fun update(d : SleepData) {
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        temphumidDetailButton = view.findViewById(R.id.temphumidDetailButton)
        temphumidDetailButton.setOnClickListener { openDetailFragment() }
        temphumidAverageTempTextView = view.findViewById(R.id.temphumidAverageTempTextView)
        temphumidAverageHumidTextView = view.findViewById(R.id.temphumidAverageHumidTextView)
        temphumidDiscomfortTextView = view.findViewById(R.id.temphumidDiscomfortTextView)
        temphumidRecommendTextView = view.findViewById(R.id.temphumidRecommendTextView)

        getAverageValues()
    }

    fun getAverageValues(){
        val aveTemp = (activity as MainActivity).currentTemp
        val aveHumid = (activity as MainActivity).currentHumid
        val aveHumidInDecimal = aveHumid/100.0
        val discomfortValue = (9.0/5.0)*aveTemp - 0.55*(1.0-aveHumidInDecimal)*(((9.0/5.0)*aveTemp) - 26.0) + 32.0
        temphumidAverageTempTextView.text = "%.1f".format(aveTemp) + "°C"
        temphumidAverageHumidTextView.text = "${aveHumid.toInt()}" + "%"
        temphumidDiscomfortTextView.text = "%.1f".format(discomfortValue) + "점"

        val discomfortText = when{
            discomfortValue > 80 -> "슴도랑 온도가 상당히 높아요!\n 에어컨이나 선풍기를 틀고 주무시는건 어떨까요?"
            discomfortValue > 75 -> "조금 찝찝할 수 있어요!\n방을 조금 시원하게 만드는건 추천드려요."
            discomfortValue > 68 -> "자면서 조금 덥다고 생각될 수 있어요.\n 얇은 이불을 쓰는걸 추천드려요."
            aveTemp == 0.0 && aveHumid == 0.0 -> "온도랑 습도가 올바르지 않아요.\n 블루투스 연결에 문제가 있는지 확인해주세요."
            aveTemp < 3 -> "여기 너무 추워요.\n꼭 히터나 전기장판을 사용하세요."
            aveTemp < 8 -> "조금 날씨가 추운편이죠?\n전기장판을 추천드릴께요."
            aveTemp < 13 -> "날씨가 조금 쌀쌀하네요.\n이불은 두꺼운걸로 바꾸셨나요?"
            else -> "오늘은 잠자기 딱 적당한 환경이네요!\n 좋은 밤 되세요."
        }

        temphumidRecommendTextView.text = discomfortText



    }




    fun openDetailFragment(){
        activity!!.supportFragmentManager
            .beginTransaction()
            .add(
                R.id.mainActivityLayout,
                TempHumidDetailFragment(),
                "DetailFragment"
            )
            .addToBackStack("DetailFragment")
            .commit()
    }

}
