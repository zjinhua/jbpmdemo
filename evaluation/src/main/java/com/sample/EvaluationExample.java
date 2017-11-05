/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import org.drools.core.marshalling.impl.ProtobufMessages.KnowledgeBase;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

public class EvaluationExample {
//	@PersistenceContext(unitName = "MySQLDBS")
//    public static EntityManager em;

    public static final void main(String[] args) {
        try {
            RuntimeManager manager = getRuntimeManager("Evaluation2.bpmn");
            System.out.println("=======RuntimeEngine============");
            RuntimeEngine runtime = manager.getRuntimeEngine(null);
            KieSession ksession = runtime.getKieSession();
            // start a new process instance
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("employee", "krisv");
            params.put("reason", "Yearly performance evaluation");
    		ksession.startProcess("com.sample.evaluation", params);

    		// complete Self Evaluation
            TaskService taskService = runtime.getTaskService();
//    		TaskService taskService = MyJBPMHelper.startTaskService();
    		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("krisv", "en-UK");
    		TaskSummary task = tasks.get(0);
    		System.out.println("'krisv' completing task "+"task.getId():"+task.getId()+":" + task.getName() + ": " + task.getDescription());
    		taskService.start(task.getId(), "krisv");
    		Map<String, Object> results = new HashMap<String, Object>();
    		results.put("performance", "exceeding");
    		taskService.complete(task.getId(), "krisv", results);
    		
    		
    		
    		// mary from PM
    		tasks = taskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
    		task = tasks.get(0);
    		System.out.println("'mary' completing task " + task.getName() + ": " + task.getDescription());
    		taskService.start(task.getId(), "mary");
    		results = new HashMap<String, Object>();
    		results.put("performance", "outstanding");
    		taskService.complete(task.getId(), "mary", results);
    		// john from HR
    		tasks = taskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
    		task = tasks.get(0);
    		System.out.println("'john' completing task " + task.getName() + ": " + task.getDescription());
    		taskService.start(task.getId(), "john");
    		results = new HashMap<String, Object>();
    		results.put("performance", "acceptable");
    		taskService.complete(task.getId(), "john", results);
    		
    		System.out.println("Process instance completed");
    		
    		manager.disposeRuntimeEngine(runtime);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(0);
    }

    private static RuntimeManager getRuntimeManager(String process) {
        // load up the knowledge base
//    	JBPMHelper.startH2Server();
    	MyJBPMHelper.setupDataSource();
    	System.out.println("EntityManagerFactory start");
//    	em.
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("MySQLDBS");
//    	 KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();  
//         kbuilder.add(ResourceFactory.newClassPathResource(process), ResourceType.BPMN2);  
//    	 RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.Factory.get()
//    				.newDefaultBuilder().entityManagerFactory(emf).knowledgeBase(kbuilder.newKieBase());
//    	 RuntimeEnvironment environment= builder.get();
    	
    	RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder().entityManagerFactory(emf)
    			.userGroupCallback(new UserGroupCallback() {
			public List<String> getGroupsForUser(String userId) {
				List<String> result = new ArrayList<String>();
				if ("mary".equals(userId)) {
					result.add("HR");
				} else if ("john".equals(userId)) {
					result.add("PM");
				}
				return result;
			}
			public boolean existsUser(String arg0) {
				return true;
			}
			public boolean existsGroup(String arg0) {
				return true;
			}
		})

                .addAsset(KieServices.Factory.get().getResources().newClassPathResource(process), ResourceType.BPMN2)
                .get();
        return RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);
    }
    
}
