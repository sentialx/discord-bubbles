package com.sential.discordbubbles

import com.facebook.rebound.SpringConfig

object SpringConfigs {
    var NOT_DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(60.0, 7.5)
    var CAPTURING = SpringConfig.fromOrigamiTensionAndFriction(100.0, 10.0)
    var DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(0.0, 5.0)
    var CONTENT_SCALE = SpringConfig.fromBouncinessAndSpeed(5.0, 40.0)
}