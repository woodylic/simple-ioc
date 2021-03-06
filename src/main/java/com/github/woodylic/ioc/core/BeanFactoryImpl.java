package com.github.woodylic.ioc.core;

import com.github.woodylic.ioc.bean.BeanDefinition;
import com.github.woodylic.ioc.bean.ConstructorArg;
import com.github.woodylic.ioc.utils.BeanUtils;
import com.github.woodylic.ioc.utils.ClassUtils;
import com.github.woodylic.ioc.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BeanFactoryImpl implements BeanFactory {

    // 存储beanName和实例化后的对象。
    private static final ConcurrentHashMap<String,Object> beanMap = new ConcurrentHashMap<>();

    // 存储对象名称和对应的数据结构的映射。
    private static final ConcurrentHashMap<String, BeanDefinition> beanDefineMap= new ConcurrentHashMap<>();


    private static final Set<String> beanNameSet = Collections.synchronizedSet(new HashSet<>());

    @Override
    public Object getBean(String name) throws Exception {

        //查找对象是否已经实例化过
        Object bean = beanMap.get(name);
        if(bean != null){
            return bean;
        }

        //如果没有实例化，那就需要调用createBean来创建对象
        bean =  createBean(beanDefineMap.get(name));

        if(bean != null) {

            //对象创建成功以后，注入对象需要的参数
            populatebean(bean);

            //再把对象存入Map中方便下次使用。
            beanMap.put(name,bean);
        }

        //结束返回
        return bean;
    }

    protected void registerBean(String name, BeanDefinition bd){
        beanDefineMap.put(name,bd);
        beanNameSet.add(name);
    }

    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        String beanName = beanDefinition.getClassName();
        Class clz = ClassUtils.loadClass(beanName);
        if(clz == null) {
            throw new Exception("can not find bean by beanName");
        }
        List<ConstructorArg> constructorArgs = beanDefinition.getConstructorArgs();
        if(constructorArgs != null && !constructorArgs.isEmpty()){
            List<Object> objects = new ArrayList<>();
            for (ConstructorArg constructorArg : constructorArgs) {
                objects.add(getBean(constructorArg.getRef()));
            }
            return BeanUtils.instanceByCglib(clz,clz.getConstructor(),objects.toArray());
        }else {
            return BeanUtils.instanceByCglib(clz,null,null);
        }
    }

    private void populatebean(Object bean) throws Exception {

        // 获取bean类的所有字段
        Field[] fields = bean.getClass().getSuperclass().getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                String beanName = field.getName();
                beanName = StringUtils.uncapitalize(beanName);

                // 如果bean类的字段名在beanNameSet中出现，则向该字段注入。
                if (beanNameSet.contains(field.getName())) {
                    Object fieldBean = getBean(beanName);
                    if (fieldBean != null) {
                        ReflectionUtils.injectField(field,bean,fieldBean);
                    }
                }
            }
        }
    }
}
