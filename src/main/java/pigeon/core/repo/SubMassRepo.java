package pigeon.core.repo;

import org.springframework.stereotype.Component;
import pigeon.core.dao.SubMassDAO;
import pigeon.core.data.SubMassDO;
import pigeon.core.entity.core.MessageMass;
import pigeon.core.entity.core.SubMass;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class SubMassRepo {
    @Resource
    private SubMassDAO dao;
    @Resource
    private MessageMassRepo messageMassRepo;
    @Resource
    private Factory factory;

    public SubMass create(long massId, int serialNum, int start, int size) {
        SubMassDO data = dao.newEmptyDataObject();
        data.setMainId(massId);
        data.setStatus(SubMass.Status.INIT);
        data.setSerialNum(serialNum);
        data.setSize(size);
        data.setStart(start);
        Long id = dao.insert(data);
        MessageMass mass = getMass(massId);
        return factory.createSubMessageMass(id, mass.data().getSpType(), mass.data().getMessageType());
    }

    public Optional<SubMass> get(Long id) {
        SubMassDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        MessageMass mass = getMass(data.getMainId());
        return Optional.of(factory.createSubMessageMass(data.getId(), mass.data().getSpType(), mass.data().getMessageType()));
    }

    public List<SubMass> listByMainId(Long mainId) {
        return dao.selectByMainId(mainId)
                .stream()
                .map(data -> {
                    MessageMass mass = getMass(data.getMainId());
                    return factory.createSubMessageMass(data.getId(), mass.data().getSpType(), mass.data().getMessageType());
                })
                .collect(Collectors.toList());
    }

    private MessageMass getMass(long id) {
        return messageMassRepo.get(id)
                .orElseThrow(() -> new MessageMassRepo.NotFoundException(id));
    }
}
