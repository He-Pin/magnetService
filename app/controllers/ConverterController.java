package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import us.sosia.magnet.service.ConverterService;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public class ConverterController extends Controller {

    public static Result convertGET(){
        return ConverterService.convertGET();
    }

    public static Result convertPOST(){
        return ConverterService.convertPOST();
    }

}
