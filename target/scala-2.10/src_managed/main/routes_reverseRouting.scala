// @SOURCE:/home/kerr/DevProjects/IdeaProjects/magnetService/conf/routes
// @HASH:9ae7f7e37a7c38fdc50b541be44ad00134a3fea6
// @DATE:Wed Dec 04 14:15:20 CST 2013

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.j._

import play.api.mvc._
import play.libs.F

import Router.queryString


// @LINE:13
// @LINE:10
// @LINE:8
// @LINE:6
package controllers {

// @LINE:13
class ReverseAssets {
    

// @LINE:13
def at(file:String): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[PathBindable[String]].unbind("file", file))
}
                                                
    
}
                          

// @LINE:10
// @LINE:8
class ReverseConverterController {
    

// @LINE:10
def convertPOST(): Call = {
   Call("POST", _prefix + { _defaultPrefix } + "convert")
}
                                                

// @LINE:8
def convertGET(): Call = {
   Call("GET", _prefix + { _defaultPrefix } + "convert")
}
                                                
    
}
                          

// @LINE:6
class ReverseApplication {
    

// @LINE:6
def index(): Call = {
   Call("GET", _prefix)
}
                                                
    
}
                          
}
                  


// @LINE:13
// @LINE:10
// @LINE:8
// @LINE:6
package controllers.javascript {

// @LINE:13
class ReverseAssets {
    

// @LINE:13
def at : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Assets.at",
   """
      function(file) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("file", file)})
      }
   """
)
                        
    
}
              

// @LINE:10
// @LINE:8
class ReverseConverterController {
    

// @LINE:10
def convertPOST : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.ConverterController.convertPOST",
   """
      function() {
      return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "convert"})
      }
   """
)
                        

// @LINE:8
def convertGET : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.ConverterController.convertGET",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "convert"})
      }
   """
)
                        
    
}
              

// @LINE:6
class ReverseApplication {
    

// @LINE:6
def index : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Application.index",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + """"})
      }
   """
)
                        
    
}
              
}
        


// @LINE:13
// @LINE:10
// @LINE:8
// @LINE:6
package controllers.ref {


// @LINE:13
class ReverseAssets {
    

// @LINE:13
def at(path:String, file:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Assets.at(path, file), HandlerDef(this, "controllers.Assets", "at", Seq(classOf[String], classOf[String]), "GET", """ Map static resources from the /public folder to the /assets URL path""", _prefix + """assets/$file<.+>""")
)
                      
    
}
                          

// @LINE:10
// @LINE:8
class ReverseConverterController {
    

// @LINE:10
def convertPOST(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.ConverterController.convertPOST(), HandlerDef(this, "controllers.ConverterController", "convertPOST", Seq(), "POST", """""", _prefix + """convert""")
)
                      

// @LINE:8
def convertGET(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.ConverterController.convertGET(), HandlerDef(this, "controllers.ConverterController", "convertGET", Seq(), "GET", """""", _prefix + """convert""")
)
                      
    
}
                          

// @LINE:6
class ReverseApplication {
    

// @LINE:6
def index(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Application.index(), HandlerDef(this, "controllers.Application", "index", Seq(), "GET", """ Home page""", _prefix + """""")
)
                      
    
}
                          
}
        
    