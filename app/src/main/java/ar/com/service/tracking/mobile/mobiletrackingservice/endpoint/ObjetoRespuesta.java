package ar.com.service.tracking.mobile.mobiletrackingservice.endpoint;

import com.gustavofao.jsonapi.Annotations.Type;
import com.gustavofao.jsonapi.Models.Resource;

import java.util.ArrayList;

/**
 * Created by miglesias on 15/07/17.
 */
@Type("objetoRespuesta")
public class ObjetoRespuesta extends Resource{

    private String title;
    private String author;

    public ObjetoRespuesta(){  }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
