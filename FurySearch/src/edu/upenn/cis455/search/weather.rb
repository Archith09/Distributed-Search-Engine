require 'open-uri'
require 'json'
open('http://api.wunderground.com/api/9ac3c90a3b722e8e/geolookup/conditions/q/' << ARGV[1] << '/' << ARGV[0] << '.json') do |f|
  json_string = f.read
  parsed_json = JSON.parse(json_string)
  location = parsed_json['location']['city']
  temp_f = parsed_json['current_observation']['temp_f']
  print "Current temperature in #{location} is: #{temp_f}\n"
end