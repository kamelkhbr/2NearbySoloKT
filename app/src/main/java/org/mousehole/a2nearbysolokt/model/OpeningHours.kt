package org.mousehole.a2nearbysolokt.model

class OpeningHours {
    // Missing list of objects here, if error comeback here first
    //public bool open_now { get; set; }

    var open_now: Boolean=false
    // places details api
    var weekday_text: Array<String>?=null
    var period: Array<Period>?=null

}