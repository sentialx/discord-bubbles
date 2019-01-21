package com.sential.discordbubbles

import com.facebook.rebound.SpringConfig

object SpringConfigs {
    var NOT_DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(60.0, 7.5)
    var CAPTURING = SpringConfig.fromBouncinessAndSpeed(8.0, 40.0)
    var CLOSE_SCALE = SpringConfig.fromBouncinessAndSpeed(7.0, 25.0)
    var DRAGGING = SpringConfig.fromOrigamiTensionAndFriction(0.0, 5.0)
    var CONTENT_SCALE = SpringConfig.fromBouncinessAndSpeed(5.0, 40.0)
}