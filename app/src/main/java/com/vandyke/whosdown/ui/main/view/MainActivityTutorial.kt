package com.vandyke.whosdown.ui.main.view

import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.vandyke.whosdown.R

class MainActivityTutorial(activity: MainActivity) : TapTargetSequence(activity) {
    init {
        targets(
                TapTarget.forView(activity.findViewById(R.id.dummyView), "Down switch", "Toggle on to indicate to your contacts that you're down (to hang out, or whatever).")
                        .transparentTarget(true)
                        .cancelable(true)
                        .textColor(R.color.white)
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(R.color.white),
                TapTarget.forView(activity.findViewById(R.id.messageCard), "Status message", "Displayed to your contacts when you're down, and updates when you press enter on the keyboard, or toggle the switch.\n\nEx:\n\"Going on an adventure, who's in?\"\n\"Bored, let me know of anything fun!\"")
                        .transparentTarget(true)
                        .cancelable(true)
                        .textColor(R.color.white)
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(R.color.white)
                        .targetRadius(18),
                TapTarget.forView(activity.findViewById(R.id.dummyView), "Contacts", "When you're down, you'll see a list of all your contacts that are also down. Try tapping and swiping on them.")
                        .cancelable(true)
                        .transparentTarget(true)
                        .textColor(R.color.white)
                        .outerCircleColor(R.color.colorAccent)
                        .targetRadius(0),
                TapTarget.forView(activity.findViewById(R.id.extrasButton), "Extras", "- Follow or block contacts\n- Share Who's Down\n- Contact the developer\n- View this tutorial again.")
                        .transparentTarget(true)
                        .cancelable(true)
                        .textColor(R.color.white)
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(R.color.white))
                .considerOuterCircleCanceled(true)
                .continueOnCancel(true)
    }
}