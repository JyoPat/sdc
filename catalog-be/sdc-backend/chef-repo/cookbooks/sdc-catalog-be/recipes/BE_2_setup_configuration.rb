# Set the cassandra replica number
cassandra_nodes = node['Nodes']['CS']
if cassandra_nodes.length <=2
   replication_factor=1
elsif cassandra_nodes.length >2 && cassandra_nodes.length <=4
   replication_factor=3
else
   replication_factor=4
end


template "titan.properties" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/titan.properties"
   source "BE-titan.properties.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :cassandra_ip             => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :cassandra_pwd            => node['cassandra'][:cassandra_password],
      :cassandra_usr            => node['cassandra'][:cassandra_user],
      :rep_factor               => replication_factor,
      :DC_NAME                  => node['cassandra'][:cluster_name]+node.chef_environment,
      :titan_connection_timeout => node['cassandra']['titan_connection_timeout'],
      :cassandra_truststore_password => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled => "#{ENV['cassandra_ssl_enabled']}"
   })
end


template "catalog-be-config" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/configuration.yaml"
   source "BE-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
   variables({
      :catalog_ip             => node['Nodes']['BE'],
      :catalog_port           => node['BE'][:http_port],
      :ssl_port               => node['BE'][:https_port],
      :cassandra_ip           => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :rep_factor             => replication_factor,
      :DC_NAME                => node['cassandra'][:cluster_name]+node.chef_environment,
      :titan_Path             => "/var/lib/jetty/config/catalog-be/",
      :socket_connect_timeout => node['cassandra']['socket_connect_timeout'],
      :socket_read_timeout    => node['cassandra']['socket_read_timeout'],
      :cassandra_pwd          => node['cassandra'][:cassandra_password],
      :cassandra_usr          => node['cassandra'][:cassandra_user],
      :cassandra_truststore_password => node['cassandra'][:truststore_password],
      :cassandra_ssl_enabled  => "#{ENV['cassandra_ssl_enabled']}",
      :dcae_fe_vip            => default['DCAE_FE_VIP']
   })
end


template "distribution-engine-configuration" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/distribution-engine-configuration.yaml"
   source "BE-distribution-engine-configuration.yaml.erb"
   owner "jetty"
   group "jetty"
   mode "0755"
end


cookbook_file "ArtifactGenerator" do
   path "#{ENV['JETTY_BASE']}/config/catalog-be/Artifact-Generator.properties"
   source "Artifact-Generator.properties"
   owner "jetty"
   group "jetty"
   mode "0755"
end
