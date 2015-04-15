package demo;

import org.springframework.stereotype.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Service
@Path("/v1/version")
@Produces("application/json")
public class VersionProvider {


    @GET
    public String getVersion() {
        return "HELLO!!";
    }
}