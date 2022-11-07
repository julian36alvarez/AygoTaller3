package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.SqsSubscription;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.ec2.*;




public class CdkAygoStack extends Stack {
    public CdkAygoStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public CdkAygoStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final Queue queue = Queue.Builder.create(this, "CdkAygoQueue")
                .visibilityTimeout(Duration.seconds(300))
                .build();

        final Topic topic = Topic.Builder.create(this, "CdkAygoTopic")
                .displayName("My First Topic Yeah")
                .build();

        topic.addSubscription(new SqsSubscription(queue));

        //Define EC2 instance type t2.micro free tier
        InstanceType instanceType = InstanceType.of(InstanceClass.T2, InstanceSize.MICRO);

        //Machine image (AMI) for the instance
        IMachineImage machineImage = MachineImage.latestAmazonLinux();

        Vpc vpc = Vpc.Builder.create(this, "VPC")
                .maxAzs(2)
                .enableDnsHostnames(true)
                .enableDnsSupport(true)
                .build();

        //Create a security group
        SecurityGroup securityGroup = SecurityGroup.Builder.create(this, "SecurityGroup")
                .vpc(vpc)
                .allowAllOutbound(true)
                .build();

        //vpc subnet public
        SubnetSelection subnetSelection = SubnetSelection.builder()
                .subnetType(SubnetType.PUBLIC)
                .build();

        //security group for instance ports
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "allow SSH access from the world");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "allow HTTP access from the world");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3000), "allow react port access from the world");
        securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3001), "allow react port access from the world");

        //load user data script
        UserData userDataSCript = UserData.forLinux();
        userDataSCript.addCommands("sudo su");
        userDataSCript.addCommands("yum update -y");
        userDataSCript.addCommands("yum install -y git");
        userDataSCript.addCommands("curl -sL https://rpm.nodesource.com/setup_16.x | sudo -E bash -");
        userDataSCript.addCommands("yum install -y nodejs");
        userDataSCript.addCommands("yum install -y npm");
        userDataSCript.addCommands("cd /home/ec2-user");
        userDataSCript.addCommands("git clone https://github.com/julian36alvarez/React-Front-Aygo-Taller-3.git");
        userDataSCript.addCommands("cd React-Front-Aygo-Taller-3");
        userDataSCript.addCommands("npm install");
        userDataSCript.addCommands("npm start");

        //Create 3 instances with public dns and load react app on each instance
        for (int i = 0; i < 3; i++) {
            Instance.Builder.create(this, "Instance-" + i)
                    .instanceType(instanceType)
                    .machineImage(machineImage)
                    .vpc(vpc)
                    .vpcSubnets(subnetSelection)
                    .allowAllOutbound(true)
                    .securityGroup(securityGroup)
                    .userData(userDataSCript)
                    //you can create your own key pair and use it here
                    .keyName("aygo-key")
                    .build();
        }


    }
}
