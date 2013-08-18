package play.mvc.results;

import play.mvc.Http;

public class FastCGIResult extends Result {

    private int serverId = 0;
    private String pathOverride = null;

    public FastCGIResult(final int serverId) {
        this.serverId = serverId;
    }

    public FastCGIResult(final int serverId, final String pathOverride) {
        this.serverId = serverId;
        this.pathOverride = pathOverride;
    }

    @Override
    public void apply(final Http.Request request, final Http.Response response) {

    }
}
