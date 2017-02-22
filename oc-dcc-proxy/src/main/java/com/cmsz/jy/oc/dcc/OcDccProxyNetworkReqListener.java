package com.cmsz.jy.oc.dcc;

import org.jdiameter.api.*;
import org.jdiameter.client.api.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by admin on 2017/2/21.
 */

@Component
public class OcDccProxyNetworkReqListener implements NetworkReqListener, EventListener<Request, Answer> {

    private static final Logger log = LoggerFactory.getLogger(OcDccProxyNetworkReqListener.class);

    private static final int commandCode = 686;
    private static final long vendorID = 0;
    private static final long acctAppId = 0;
    private static final long applicationID = 333333;
    private ApplicationId authAppId = ApplicationId.createByAuthAppId(applicationID);

    @Autowired
    private OcDccStack ocDccStack;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;

    @PostConstruct
    public void init() {
        ocDccStack.addReqListener(this, authAppId);
        cache = cacheManager.getCache("proxycache");
    }

    @Override
    public void receivedSuccessMessage(Request request, Answer answer) {
        Session session = null;
        Long hopbyhopid = cache.get(makeRoutingKey(request), Long.class);

        log.info("++++ recive Success Message :" + hopbyhopid + "  send hopid :" + request.getHopByHopIdentifier());

        ((IMessage)answer).setHopByHopIdentifier(hopbyhopid);
        try {
            session = ocDccStack.getNewSession(answer.getSessionId());
            session.send(answer);
        } catch (IllegalDiameterStateException e) {
            e.printStackTrace();
        } catch (RouteException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (OverloadException e) {
            e.printStackTrace();
        }finally {
            if (session != null)
                session.release();
        }
    }

    @Override
    public void timeoutExpired(Request request) {

    }

    @Override
    public Answer processRequest(Request request) {
        long hopbyhopid = request.getHopByHopIdentifier();
        log.info("+++++ Receive  hopid: " + hopbyhopid);
        Session session = null;
        try {
            session = ocDccStack.getNewSession(request.getSessionId());
            session.send(request, this);
        } catch (IllegalDiameterStateException e) {
            e.printStackTrace();
        } catch (RouteException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        } catch (OverloadException e) {
            e.printStackTrace();
        }finally {
            if (session !=null)
                session.release();
        }
        cache.put(makeRoutingKey(request),hopbyhopid);
        log.info("+++++ Receive  hopid: " + hopbyhopid + " send to hopid : " + request.getHopByHopIdentifier());
        return null;
    }


    private String makeRoutingKey(Message message) {
        String sessionId = message.getSessionId();
        return new StringBuilder(sessionId != null ? sessionId : "null").append(message.getEndToEndIdentifier())
                .append(message.getHopByHopIdentifier()).toString();
    }

}
