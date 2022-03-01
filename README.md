# Pigeon Core

Defines core domain model and extension points of pigeon service.

[CHANGELOG.md](./CHANGELOG.md)

## References

### Models

<a href="https://ibb.co/0JjGdFJ"><img src="https://i.ibb.co/ryGw1cy/image.png" height=400 width=500 alt="image" border="0"></a>

<a href="https://ibb.co/V2c0BDH"><img src="https://i.ibb.co/dGwsK6b/image.png" height=250 width=400 alt="image" border="0"></a>

### Events

- `Message.DeliverEvent`: emits on a single message deliver finished(only non-realtime message & non-batch mode)
- `Message.SentEvent`: emits on a single message sent(only non-batch mode)
- `MessageMass.StartDeliverEvent`: emits on a mass start deliver
- `MessageMass.DeliveredEvent`: emits on a mass finish deliver
- `SubMass.DeliveredEvent`: emits on a sub mass finish deliver
 
### Extension Points

#### Factory

- `MassTacticFactory`: determine how to create a mass tactic
- `MessageFactory`: determine how to create a message
- `MessageMassFactory`: determine how to create a message mass
- `MessageTemplateFactory`: determine how to create a message template
- `ServiceProviderFactory`: determine how to create a service provider
- `SubMassFactory`: determine how to create a sub mass
- `ThirdAccountFactory`: determine how to create a third account
- `UserFactory`: determine how to create a user

#### Apis

Extend an api is very easy.

```java
// FooController.java
@RestController
@RequestMapping("plugins/foo")
public class FooController {
    @GetMapping
    public String index() {
        // because of repo is defined in main app context and FooController is defined in your plugin context,
        // so you can only obtain it via `PigeonContext` instead of @Autowired
        MessageTemplateRepo repo = PigeonContext.getRepo(MessageTemplateRepo.class);
        MessageTemplate template = repo.getOrThrow(4);
        return template.data().getContent();
    }
}
```

Yes. you may now find that many familiar annotations and naming conventions.
that's because pigeon is compatible entirely with Spring MVC(in fact, pigeon provide a `PluginMappingChainHandlerMapping` that allow your controllers to be loaded on main app startup).

> tips: if there is a conflict, priority of apis defined in plugin is always lower than in main.

#### Others

- `PluginDocs`: provides info extended by your plugin. main app will show it in swagger docs dynamically.
