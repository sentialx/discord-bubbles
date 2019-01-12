package com.sential.discordbubbles

import com.facebook.rebound.SpringConfig

object SpringConfigs {
    var NOT_DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(190.0, 20.0)
    var CAPTURING = SpringConfig.fromOrigamiTensionAndFriction(100.0, 10.0)
    var DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(0.0, 5.0)
}