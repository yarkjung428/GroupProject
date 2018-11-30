package com.egci428.groupproject

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.egci428.groupproject.MyService.Companion.numSteps
import kotlinx.android.synthetic.main.fragment_walk_detail.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class WalkDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    var tv_avgsteps: TextView? = null
    var tv_avgdistance: TextView? = null
    var tv_avgcal: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_walk_detail, container, false)
        var tv_date = view.findViewById(R.id.tv_date) as TextView
        tv_avgsteps = view.findViewById(R.id.tv_avgsteps) as TextView
        tv_avgdistance = view.findViewById(R.id.tv_avgdistance) as TextView
        tv_avgcal = view.findViewById(R.id.tv_avgcal) as TextView

        var wakeupTimeData = MyService.dataList[MyService.dataList.size - 1].wakeupTime.split("_")
        tv_date!!.text = wakeupTimeData.get(0)

        setaveragedata()
        return view
    }

    fun setaveragedata(){
        var numStep = 0
        var dataCount = 0

        for(i in 1..MyService.dataList.size) {
             if(MyService.dataList[i-1].numSteps>0){
                 numStep = numStep + MyService.dataList[i-1].numSteps
                 dataCount++
             }
        }
        var distance = String.format("%.3f", numSteps*0.000762)
        var cal = String.format("%.3f", numSteps*0.045)

        tv_avgsteps!!.text = (numStep/dataCount).toString()
        tv_avgdistance!!.text = distance
        tv_avgcal!!.text = cal
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onResume() {
        super.onResume()
        tv_steps.setText(numSteps.toString())
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                WalkDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
