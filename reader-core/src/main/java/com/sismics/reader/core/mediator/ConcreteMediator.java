package com.sismics.reader.core.mediator;

import com.sismics.reader.core.service.FeedService;
import com.sismics.reader.core.model.context.AppContext;

public  class ConcreteMediator implements Mediator {
    private AppContext appContext;
    private FeedService feedService;

    public ConcreteMediator(AppContext appContext, FeedService feedService) {
        this.appContext = appContext;
        this.feedService = feedService;
    }

    @Override
    public void notify(Object sender, Object event) {
        if (sender instanceof FeedService) {
           appContext.getInstance().getEventBus().post(event);
        }
    }
}