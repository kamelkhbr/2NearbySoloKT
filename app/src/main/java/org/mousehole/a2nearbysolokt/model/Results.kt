package org.mousehole.a2nearbysolokt.model

class Results {


    var icon:String?=null
    var photos:Array<Photos>?=null
    var id: String?=null
    var places_id:String?=null
    var price_level:Int=0
    var rating:Double=0.0
    var reference:String?=null
    var scope:String?=null
    var types:Array<String>?=null
    var vicinity:String?=null
    var opening_hours: OpeningHours?=null

    // Places details API

    var address_components: Array<AddressComponent>?=null
    var adr_address: String?=null
    var business_status: String?=null
    var formatted_address: String?=null
    var formatted_phone_number: String?=null
    var geometry: Geometry?=null
    var international_phone_number:String?=null
    var name: String?=null
    var reviews: Array<Review>?=null
    var url:String?=null
    var user_ratings_total: Int=0
    var utc_offset:Int=0
    var website:String?=null



}
