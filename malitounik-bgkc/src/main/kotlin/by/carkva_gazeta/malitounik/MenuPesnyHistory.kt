package by.carkva_gazeta.malitounik

import androidx.fragment.app.Fragment

abstract class MenuPesnyHistory : Fragment() {
    abstract fun cleanFullHistory()
    abstract fun cleanHistory(position: Int)
}